package innercircle.commerce.product.admin.application.scheduler;

import innercircle.commerce.product.core.application.repository.ImageDeletionTargetRepository;
import innercircle.commerce.product.core.domain.ImageDeletionTarget;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import innercircle.commerce.product.infra.s3.S3UrlHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("이미지 삭제 스케줄러 테스트")
class ImageDeletionSchedulerTest {

    @Mock
    private ImageDeletionTargetRepository imageDeletionTargetRepository;

    @Mock
    private S3ImageStore s3ImageStore;

    @Mock
    private S3UrlHelper s3UrlHelper;

    private ImageDeletionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ImageDeletionScheduler(imageDeletionTargetRepository, s3ImageStore, s3UrlHelper);
        ReflectionTestUtils.setField(scheduler, "batchSize", 100);
        ReflectionTestUtils.setField(scheduler, "maxRetries", 3);
        ReflectionTestUtils.setField(scheduler, "retryDelayHours", 1);
    }

    @Nested
    @DisplayName("이미지 삭제 스케줄링")
    class DeleteScheduledImages {

        @Test
        @DisplayName("삭제 대상이 없으면 아무것도 처리하지 않는다")
        void 삭제_대상_없음() {
            // given
            given(imageDeletionTargetRepository.findAllByScheduledDeletionAtBeforeAndRetryCountLessThanEqual(
                    any(LocalDateTime.class), anyInt(), any(Pageable.class)))
                    .willReturn(List.of());

            // when
            scheduler.deleteScheduledImages();

            // then
            verify(s3ImageStore, never()).delete(any(String.class));
        }

        @Test
        @DisplayName("단일 이미지 삭제가 성공적으로 처리된다")
        void 단일_이미지_삭제_성공() {
            // given
            String imageUrl = "https://bucket.s3.amazonaws.com/test/image.jpg";
            String s3Key = "test/image.jpg";
            
            ImageDeletionTarget target = ImageDeletionTarget.create(imageUrl, 0);
            List<ImageDeletionTarget> targets = List.of(target);

            given(imageDeletionTargetRepository.findAllByScheduledDeletionAtBeforeAndRetryCountLessThanEqual(
                    any(LocalDateTime.class), eq(3), any(Pageable.class)))
                    .willReturn(targets)
                    .willReturn(List.of()); // 두 번째 호출에서는 빈 리스트

            given(s3UrlHelper.extractKeyFromUrl(imageUrl)).willReturn(s3Key);
            willDoNothing().given(s3ImageStore).delete(s3Key);

            // when
            scheduler.deleteScheduledImages();

            // then
            verify(s3ImageStore).delete(s3Key);
            verify(imageDeletionTargetRepository).delete(target);
        }

        @Test
        @DisplayName("이미지 삭제 실패 시 재시도 카운트가 증가한다")
        void 이미지_삭제_실패_재시도() {
            // given
            String imageUrl = "https://bucket.s3.amazonaws.com/test/image.jpg";
            String s3Key = "test/image.jpg";
            
            ImageDeletionTarget target = ImageDeletionTarget.create(imageUrl, 0);
            List<ImageDeletionTarget> targets = List.of(target);

            given(imageDeletionTargetRepository.findAllByScheduledDeletionAtBeforeAndRetryCountLessThanEqual(
                    any(LocalDateTime.class), eq(3), any(Pageable.class)))
                    .willReturn(targets)
                    .willReturn(List.of()); // 두 번째 호출에서는 빈 리스트

            given(s3UrlHelper.extractKeyFromUrl(imageUrl)).willReturn(s3Key);
            willThrow(new RuntimeException("S3 삭제 실패")).given(s3ImageStore).delete(s3Key);

            // when
            scheduler.deleteScheduledImages();

            // then
            verify(s3ImageStore).delete(s3Key);
            verify(imageDeletionTargetRepository).save(target);
            verify(imageDeletionTargetRepository, never()).delete(target);
        }

        @Test
        @DisplayName("배치 처리로 여러 이미지가 순차적으로 처리된다")
        void 배치_처리_여러_이미지() {
            // given
            ImageDeletionTarget target1 = ImageDeletionTarget.create("https://bucket.s3.amazonaws.com/test/image1.jpg", 0);
            ImageDeletionTarget target2 = ImageDeletionTarget.create("https://bucket.s3.amazonaws.com/test/image2.jpg", 0);
            List<ImageDeletionTarget> targets = List.of(target1, target2);

            given(imageDeletionTargetRepository.findAllByScheduledDeletionAtBeforeAndRetryCountLessThanEqual(
                    any(LocalDateTime.class), eq(3), any(Pageable.class)))
                    .willReturn(targets)
                    .willReturn(List.of()); // 두 번째 호출에서는 빈 리스트

            given(s3UrlHelper.extractKeyFromUrl("https://bucket.s3.amazonaws.com/test/image1.jpg")).willReturn("test/image1.jpg");
            given(s3UrlHelper.extractKeyFromUrl("https://bucket.s3.amazonaws.com/test/image2.jpg")).willReturn("test/image2.jpg");

            // when
            scheduler.deleteScheduledImages();

            // then
            verify(s3ImageStore).delete("test/image1.jpg");
            verify(s3ImageStore).delete("test/image2.jpg");
            verify(imageDeletionTargetRepository).delete(target1);
            verify(imageDeletionTargetRepository).delete(target2);
        }
    }

    @Nested
    @DisplayName("실패한 대상 정리")
    class CleanupFailedTargets {

        @Test
        @DisplayName("실패한 대상 정리가 정상적으로 실행된다")
        void 실패한_대상_정리() {
            // when
            scheduler.cleanupFailedTargets();

            // then
            // 현재 구현은 로그만 출력하므로 예외가 발생하지 않는 것을 확인
            // 실제 정리 로직이 구현되면 해당 메서드 호출을 검증
        }
    }

    @Nested
    @DisplayName("통계 로깅")
    class LogStatistics {

        @Test
        @DisplayName("삭제 대기 통계가 정상적으로 수집된다")
        void 삭제_대기_통계() {
            // given
            List<ImageDeletionTarget> pendingTargets = List.of(
                    ImageDeletionTarget.create("https://example.com/image1.jpg", 0),
                    ImageDeletionTarget.create("https://example.com/image2.jpg", 0)
            );

            given(imageDeletionTargetRepository.findAllByScheduledDeletionAtBefore(any(LocalDateTime.class)))
                    .willReturn(pendingTargets);

            // when
            scheduler.logStatistics();

            // then
            verify(imageDeletionTargetRepository).findAllByScheduledDeletionAtBefore(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("통계 수집 중 예외가 발생해도 스케줄러가 중단되지 않는다")
        void 통계_수집_예외_처리() {
            // given
            given(imageDeletionTargetRepository.findAllByScheduledDeletionAtBefore(any(LocalDateTime.class)))
                    .willThrow(new RuntimeException("DB 연결 실패"));

            // when & then
            // 예외가 발생해도 메서드가 정상 완료되어야 함
            scheduler.logStatistics();
        }
    }
}