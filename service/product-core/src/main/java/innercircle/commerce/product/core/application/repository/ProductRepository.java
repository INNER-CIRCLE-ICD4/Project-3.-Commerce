package innercircle.commerce.product.core.application.repository;

import innercircle.commerce.product.core.domain.Product;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository {
    /**
     * 상품을 저장합니다.
     * 
     * @param product 저장할 상품
     * @return 저장된 상품
     */
    Product save(Product product);
    
    /**
     * 상품명으로 상품 존재 여부를 확인합니다.
     * 
     * @param name 상품명
     * @return 존재 여부
     */
    boolean existsByName(String name);
    
    /**
     * 상품 ID로 상품을 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 상품 (Optional)
     */
    Optional<Product> findById(Long productId);
    
    /**
     * 특정 상품 ID를 제외하고 상품명 중복을 확인합니다.
     * 
     * @param name 상품명
     * @param excludeProductId 제외할 상품 ID
     * @return 중복 여부
     */
    boolean existsByNameAndIdNot(String name, Long excludeProductId);
}
