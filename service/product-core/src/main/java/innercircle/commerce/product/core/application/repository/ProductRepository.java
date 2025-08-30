package innercircle.commerce.product.core.application.repository;

import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    /**
     * 전체 상품 목록을 페이징하여 조회합니다.
     * 
     * @param pageable 페이징 정보
     * @return 상품 목록
     */
    Page<Product> findAll(Pageable pageable);
    
    /**
     * 상태별 상품 목록을 페이징하여 조회합니다.
     * 
     * @param status 상품 상태
     * @param pageable 페이징 정보
     * @return 상품 목록
     */
    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);
    
    /**
     * 카테고리별 상품 목록을 페이징하여 조회합니다.
     * 
     * @param categoryId 카테고리 ID
     * @param pageable 페이징 정보
     * @return 상품 목록
     */
    Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable);
    
    /**
     * 상태와 카테고리 조건으로 상품 목록을 페이징하여 조회합니다.
     * 
     * @param status 상품 상태
     * @param categoryId 카테고리 ID
     * @param pageable 페이징 정보
     * @return 상품 목록
     */
    Page<Product> findAllByStatusAndCategoryId(ProductStatus status, Long categoryId, Pageable pageable);
    
    /**
     * 다양한 조건으로 상품 목록을 페이징하여 조회합니다.
     * 
     * @param status 상품 상태 (null 허용)
     * @param categoryId 카테고리 ID (null 허용)
     * @param pageable 페이징 정보
     * @return 상품 목록
     */
    Page<Product> findProducts(ProductStatus status, Long categoryId, Pageable pageable);
}
