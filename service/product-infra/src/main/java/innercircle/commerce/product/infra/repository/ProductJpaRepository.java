package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.infra.entity.ProductJpaEntity;
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
}