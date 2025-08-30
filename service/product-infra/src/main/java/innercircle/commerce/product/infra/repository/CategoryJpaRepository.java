package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.infra.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Category JPA Repository
 */
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {
}