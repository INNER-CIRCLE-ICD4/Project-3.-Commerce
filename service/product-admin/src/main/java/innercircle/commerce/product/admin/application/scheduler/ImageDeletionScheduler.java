package innercircle.commerce.product.admin.application.scheduler;

import innercircle.commerce.product.core.application.repository.ImageDeletionTargetRepository;
import innercircle.commerce.product.core.domain.ImageDeletionTarget;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import innercircle.commerce.product.infra.s3.S3UrlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageDeletionScheduler {

    private final ImageDeletionTargetRepository imageDeletionTargetRepository;
    private final S3ImageStore s3ImageStore;
    private final S3UrlHelper s3UrlHelper;

    @Value("${image.deletion.scheduler.batch-size:100}")
    private int batchSize;

    @Value("${image.deletion.scheduler.max-retries:3}")
    private int maxRetries;

    @Value("${image.deletion.scheduler.retry-delay-hours:1}")
    private int retryDelayHours;

    @Scheduled(cron = "${image.deletion.scheduler.cron:0 0 4 * * *}")
    @Transactional
    public void deleteScheduledImages() {
        log.info("이미지 지연 삭제 스케줄러 시작");
        
        int totalProcessed = 0;
        int totalDeleted = 0;
        int totalFailed = 0;
        
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, batchSize);
        
        List<ImageDeletionTarget> targets;
        do {
            targets = imageDeletionTargetRepository
                    .findAllByScheduledDeletionAtBeforeAndRetryCountLessThanEqual(now, maxRetries, pageable);
            
            if (!targets.isEmpty()) {
                log.info("배치 처리 시작: {} 개 대상", targets.size());
                
                for (ImageDeletionTarget target : targets) {
                    boolean deleted = processSingleImage(target);
                    totalProcessed++;
                    
                    if (deleted) {
                        totalDeleted++;
                    } else {
                        totalFailed++;
                    }
                }
            }
        } while (targets.size() == batchSize);
        
        // 실패한 대상들 정리 (선택적)
        cleanupFailedTargets();
        
        log.info("이미지 지연 삭제 스케줄러 완료: 처리={}, 삭제 성공={}, 실패={}", 
                totalProcessed, totalDeleted, totalFailed);
    }

    @Transactional
    private boolean processSingleImage(ImageDeletionTarget target) {
        try {
            log.debug("이미지 삭제 처리 시작: id={}, url={}, retryCount={}", 
                    target.getId(), target.getImageUrl(), target.getRetryCount());
            
            String s3Key = s3UrlHelper.extractKeyFromUrl(target.getImageUrl());
            s3ImageStore.delete(s3Key);
            
            // 삭제 성공 시 대상 제거
            imageDeletionTargetRepository.delete(target);
            
            log.debug("이미지 삭제 성공: id={}, url={}", target.getId(), target.getImageUrl());
            return true;
            
        } catch (Exception e) {
            log.warn("이미지 삭제 실패: id={}, url={}, retryCount={}, error={}", 
                    target.getId(), target.getImageUrl(), target.getRetryCount(), e.getMessage());
            
            handleDeletionFailure(target);
            return false;
        }
    }

    @Transactional
    private void handleDeletionFailure(ImageDeletionTarget target) {
        try {
            target.incrementRetryCount();
            
            if (target.getRetryCount() <= maxRetries) {
                // 재시도 가능한 경우 지연 후 재시도 예약
                target.rescheduleAfterFailure(retryDelayHours);
                imageDeletionTargetRepository.save(target);
                log.debug("이미지 삭제 재시도 예약: id={}, retryCount={}, nextTry={}", 
                        target.getId(), target.getRetryCount(), target.getScheduledDeletionAt());
            } else {
                // 최대 재시도 초과 시에도 DB에 유지 (정리는 별도 스케줄러에서)
                imageDeletionTargetRepository.save(target);
                log.warn("이미지 삭제 최대 재시도 초과: id={}, url={}, retryCount={}", 
                        target.getId(), target.getImageUrl(), target.getRetryCount());
            }
        } catch (Exception e) {
            log.error("삭제 실패 처리 중 오류 발생: id={}, error={}", target.getId(), e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${image.deletion.cleanup.cron:0 0 2 * * SUN}")
    @Transactional
    public void cleanupFailedTargets() {
        log.info("실패한 이미지 삭제 대상 정리 시작");
        
        try {
            // 7일 이전에 생성되고 최대 재시도를 초과한 대상들을 정리
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7);
            // JPA Repository에 해당 메서드가 있다고 가정
            // List<ImageDeletionTarget> failedTargets = imageDeletionTargetRepository
            //         .findFailedTargetsForCleanup(cutoffTime, maxRetries);
            
            // TODO: 실패한 대상들에 대한 정리 로직 구현
            // 예: 별도 테이블로 이동, 알림 발송, 로그 기록 등
            
            log.info("실패한 이미지 삭제 대상 정리 완료");
            
        } catch (Exception e) {
            log.error("실패한 이미지 삭제 대상 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${image.deletion.stats.cron:0 0 6 * * *}")
    public void logStatistics() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<ImageDeletionTarget> pendingTargets = imageDeletionTargetRepository
                    .findAllByScheduledDeletionAtBefore(now);
            
            long totalPending = pendingTargets.size();
            long readyForDeletion = pendingTargets.stream()
                    .filter(target -> target.getRetryCount() <= maxRetries)
                    .count();
            long failedTargets = totalPending - readyForDeletion;
            
            log.info("이미지 삭제 대기 현황: 전체={}, 삭제 대기={}, 실패={}", 
                    totalPending, readyForDeletion, failedTargets);
                    
        } catch (Exception e) {
            log.error("이미지 삭제 통계 수집 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}