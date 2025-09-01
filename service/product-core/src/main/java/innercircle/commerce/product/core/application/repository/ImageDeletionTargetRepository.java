package innercircle.commerce.product.core.application.repository;

import innercircle.commerce.product.core.domain.ImageDeletionTarget;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImageDeletionTargetRepository {
    
    /**
     * 삭제 대상을 저장합니다.
     * 
     * @param target 저장할 삭제 대상
     * @return 저장된 삭제 대상
     */
    ImageDeletionTarget save(ImageDeletionTarget target);
    
    /**
     * 여러 삭제 대상을 일괄 저장합니다.
     * 
     * @param targets 저장할 삭제 대상 목록
     * @return 저장된 삭제 대상 목록
     */
    List<ImageDeletionTarget> saveAll(List<ImageDeletionTarget> targets);
    
    /**
     * ID로 삭제 대상을 조회합니다.
     * 
     * @param id 삭제 대상 ID
     * @return 삭제 대상 (Optional)
     */
    Optional<ImageDeletionTarget> findById(Long id);
    
    /**
     * 예정된 삭제 시간이 지난 삭제 대상들을 페이징하여 조회합니다.
     * 
     * @param now 현재 시간
     * @param pageable 페이징 정보
     * @return 삭제 대상 목록
     */
    List<ImageDeletionTarget> findAllByScheduledDeletionAtBefore(LocalDateTime now, Pageable pageable);
    
    /**
     * 예정된 삭제 시간이 지난 삭제 대상들을 조회합니다.
     * 
     * @param now 현재 시간
     * @return 삭제 대상 목록
     */
    List<ImageDeletionTarget> findAllByScheduledDeletionAtBefore(LocalDateTime now);
    
    /**
     * 특정 재시도 횟수 이하인 삭제 대상들 중 예정된 삭제 시간이 지난 것들을 조회합니다.
     * 
     * @param now 현재 시간
     * @param maxRetryCount 최대 재시도 횟수
     * @param pageable 페이징 정보
     * @return 삭제 대상 목록
     */
    List<ImageDeletionTarget> findAllByScheduledDeletionAtBeforeAndRetryCountLessThanEqual(
            LocalDateTime now, int maxRetryCount, Pageable pageable);
    
    /**
     * 삭제 대상을 삭제합니다.
     * 
     * @param target 삭제할 대상
     */
    void delete(ImageDeletionTarget target);
    
    /**
     * ID로 삭제 대상을 삭제합니다.
     * 
     * @param id 삭제할 대상 ID
     */
    void deleteById(Long id);
    
    /**
     * 여러 삭제 대상을 일괄 삭제합니다.
     * 
     * @param targets 삭제할 대상 목록
     */
    void deleteAll(List<ImageDeletionTarget> targets);
    
    /**
     * 특정 이미지 URL의 삭제 대상이 존재하는지 확인합니다.
     * 
     * @param imageUrl 이미지 URL
     * @return 존재 여부
     */
    boolean existsByImageUrl(String imageUrl);
    
    /**
     * 특정 이미지 URL의 삭제 대상을 조회합니다.
     * 
     * @param imageUrl 이미지 URL
     * @return 삭제 대상 (Optional)
     */
    Optional<ImageDeletionTarget> findByImageUrl(String imageUrl);
}