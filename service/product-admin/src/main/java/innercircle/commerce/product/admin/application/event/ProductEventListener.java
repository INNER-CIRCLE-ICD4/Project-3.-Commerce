package innercircle.commerce.product.admin.application.event;

import innercircle.commerce.product.core.domain.event.ProductDeletedEvent;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import innercircle.commerce.product.infra.s3.S3UrlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 상품 관련 도메인 이벤트를 처리하는 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final S3ImageStore s3ImageStore;
    private final S3UrlHelper s3UrlHelper;

    /**
     * 상품 삭제 이벤트를 처리합니다.
     * S3에서 해당 상품의 이미지 파일들을 비동기적으로 삭제합니다.
     *
     * @param event 상품 삭제 이벤트
     */
    @Async
    @EventListener
    public void onProductDeleted(ProductDeletedEvent event) {
        log.info("상품 삭제 이벤트 처리 시작: productId={}, imageCount={}", 
                 event.getProductId(), event.getImageUrls().size());

        if (CollectionUtils.isEmpty(event.getImageUrls())) {
            log.info("삭제할 이미지가 없습니다: productId={}", event.getProductId());
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (String imageUrl : event.getImageUrls()) {
            try {
                String s3Key = s3UrlHelper.extractKeyFromUrl(imageUrl);
                s3ImageStore.delete(s3Key);
                successCount++;
                log.debug("이미지 삭제 성공: productId={}, imageUrl={}", event.getProductId(), imageUrl);
                
            } catch (Exception e) {
                failCount++;
                log.warn("이미지 삭제 실패: productId={}, imageUrl={}, error={}", 
                         event.getProductId(), imageUrl, e.getMessage(), e);
                
                // TODO: 실패한 이미지에 대한 재시도 로직 또는 Dead Letter Queue 처리 필요
            }
        }

        log.info("상품 삭제 이벤트 처리 완료: productId={}, 성공={}, 실패={}", 
                 event.getProductId(), successCount, failCount);
    }

}