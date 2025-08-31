package innercircle.commerce.product.core.application.repository;

import innercircle.commerce.product.core.domain.Brand;

import java.util.Optional;

public interface BrandRepository {
    Brand save(Brand brand);
    Optional<Brand> findById(Long brandId);
    boolean existsById(Long brandId);
}
