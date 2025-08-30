package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.infra.entity.ProductJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Product JPA Repository
 */
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    /**
     * 상품명으로 상품 존재 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 특정 상품 ID를 제외하고 상품명 중복 확인
     */
    boolean existsByNameAndIdNot(String name, Long excludeProductId);
    
    /**
     * 상태별 상품 목록을 페이징하여 조회
     */
    Page<ProductJpaEntity> findAllByStatus(ProductStatus status, Pageable pageable);
    
    /**
     * 카테고리별 상품 목록을 페이징하여 조회
     */
    Page<ProductJpaEntity> findAllByCategoryId(Long categoryId, Pageable pageable);
    
    /**
     * 상태와 카테고리 조건으로 상품 목록을 페이징하여 조회
     */
    Page<ProductJpaEntity> findAllByStatusAndCategoryId(ProductStatus status, Long categoryId, Pageable pageable);
}