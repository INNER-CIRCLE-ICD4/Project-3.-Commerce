package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.event.ProductDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 삭제 UseCase
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductDeleteUseCase {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 상품을 삭제합니다.
     *
     * @param productId 삭제할 상품 ID
     * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
     * @throws IllegalArgumentException 이미 삭제된 상품인 경우
     */
    public void deleteProduct(Long productId) {
        Product product = findProductById(productId);

        // 이미지 URL 목록 수집 (이벤트 발행용)
        List<String> imageUrls = !CollectionUtils.isEmpty(product.getImages()) ? 
                product.getImages().stream()
                        .map(image -> image.getUrl())
                        .collect(Collectors.toList()) :
                List.of();

        // 상품 삭제
        product.delete();
        productRepository.save(product);

        // 삭제 이벤트 발행
        ProductDeletedEvent event = new ProductDeletedEvent(productId, imageUrls);
        eventPublisher.publishEvent(event);
        
        // TODO: 검색(Search) 서비스 등 외부 서비스에서 해당 상품을 제거하는 로직 연동 필요
    }

    /**
     * 상품 ID로 상품을 조회합니다.
     *
     * @param productId 상품 ID
     * @return 조회된 상품
     * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
     */
    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}