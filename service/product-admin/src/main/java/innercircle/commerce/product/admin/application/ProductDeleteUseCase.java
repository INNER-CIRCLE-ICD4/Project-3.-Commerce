package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.core.application.repository.ImageDeletionTargetRepository;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.ImageDeletionTarget;
import innercircle.commerce.product.core.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final ImageDeletionTargetRepository imageDeletionTargetRepository;
    
    @Value("${image.deletion.delay-days:30}")
    private int imageDeletionDelayDays;

    /**
     * 상품을 삭제합니다.
     *
     * @param productId 삭제할 상품 ID
     * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
     * @throws IllegalArgumentException 이미 삭제된 상품인 경우
     */
    public void deleteProduct(Long productId) {
        Product product = findProductById(productId);

        // 이미지 지연 삭제 대상으로 등록
        if (!CollectionUtils.isEmpty(product.getImages())) {
            List<ImageDeletionTarget> deletionTargets = product.getImages().stream()
                    .map(image -> ImageDeletionTarget.create(image.getUrl(), imageDeletionDelayDays))
                    .collect(Collectors.toList());
            
            imageDeletionTargetRepository.saveAll(deletionTargets);
        }

        // 상품 논리적 삭제
        product.delete();
        productRepository.save(product);
        
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