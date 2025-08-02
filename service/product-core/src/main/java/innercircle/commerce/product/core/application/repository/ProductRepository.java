package innercircle.commerce.product.core.application.repository;

import innercircle.commerce.product.core.domain.entity.Product;

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
}
