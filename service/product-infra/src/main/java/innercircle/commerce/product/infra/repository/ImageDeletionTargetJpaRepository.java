package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.infra.entity.ImageDeletionTargetJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ImageDeletionTargetJpaRepository extends JpaRepository<ImageDeletionTargetJpaEntity, Long> {

    /**
     * 예정된 삭제 시간이 지난 삭제 대상들을 페이징하여 조회
     */
    List<ImageDeletionTargetJpaEntity> findAllByScheduledDeletionAtBefore(LocalDateTime now, Pageable pageable);

    /**
     * 예정된 삭제 시간이 지난 삭제 대상들을 조회
     */
    List<ImageDeletionTargetJpaEntity> findAllByScheduledDeletionAtBefore(LocalDateTime now);

    /**
     * 특정 재시도 횟수 이하인 삭제 대상들 중 예정된 삭제 시간이 지난 것들을 조회
     */
    List<ImageDeletionTargetJpaEntity> findAllByScheduledDeletionAtBeforeAndRetryCountLessThanEqual(
            LocalDateTime now, Integer maxRetryCount, Pageable pageable);

    /**
     * 특정 이미지 URL의 삭제 대상이 존재하는지 확인
     */
    boolean existsByImageUrl(String imageUrl);

    /**
     * 특정 이미지 URL의 삭제 대상을 조회
     */
    Optional<ImageDeletionTargetJpaEntity> findByImageUrl(String imageUrl);

    /**
     * 예정된 삭제 시간이 지나고 재시도 횟수가 최대치 이하인 대상들을 조회
     * 생성일 기준으로 정렬하여 오래된 것부터 처리
     */
    @Query("SELECT t FROM ImageDeletionTargetJpaEntity t " +
           "WHERE t.scheduledDeletionAt < :now " +
           "AND t.retryCount <= :maxRetryCount " +
           "ORDER BY t.createdAt ASC")
    List<ImageDeletionTargetJpaEntity> findTargetsForDeletion(
            @Param("now") LocalDateTime now,
            @Param("maxRetryCount") Integer maxRetryCount,
            Pageable pageable);

    /**
     * 특정 시간 이전에 생성되고 최대 재시도 횟수를 초과한 실패한 대상들을 조회
     */
    @Query("SELECT t FROM ImageDeletionTargetJpaEntity t " +
           "WHERE t.createdAt < :cutoffTime " +
           "AND t.retryCount > :maxRetryCount")
    List<ImageDeletionTargetJpaEntity> findFailedTargetsForCleanup(
            @Param("cutoffTime") LocalDateTime cutoffTime,
            @Param("maxRetryCount") Integer maxRetryCount);
}