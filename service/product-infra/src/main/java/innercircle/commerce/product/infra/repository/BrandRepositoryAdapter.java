package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.core.application.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * BrandRepository JPA 구현체
 */
@Repository
@RequiredArgsConstructor
public class BrandRepositoryAdapter implements BrandRepository {
	private final BrandJpaRepository brandJpaRepository;

	@Override
	public boolean existsById (Long brandId) {
		return brandJpaRepository.existsById(brandId);
	}
}