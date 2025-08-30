package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.core.application.repository.BrandRepository;
import innercircle.commerce.product.core.domain.Brand;
import innercircle.commerce.product.infra.entity.BrandJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * BrandRepository JPA 구현체
 */
@Repository
@RequiredArgsConstructor
public class BrandRepositoryAdapter implements BrandRepository {
	private final BrandJpaRepository brandJpaRepository;

	@Override
	public Brand save(Brand brand) {
		BrandJpaEntity entity = BrandJpaEntity.from(brand);
		BrandJpaEntity savedEntity = brandJpaRepository.save(entity);
		return savedEntity.toDomain();
	}

	@Override
	public Optional<Brand> findById(Long brandId) {
		return brandJpaRepository.findById(brandId)
				.map(BrandJpaEntity::toDomain);
	}

	public boolean existsById (Long brandId) {
		return brandJpaRepository.existsById(brandId);
	}
}