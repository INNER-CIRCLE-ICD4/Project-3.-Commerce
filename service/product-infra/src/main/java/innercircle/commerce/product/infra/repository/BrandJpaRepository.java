package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.infra.entity.BrandJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Brand JPA Repository
 */
public interface BrandJpaRepository extends JpaRepository<BrandJpaEntity, Long> {
}
