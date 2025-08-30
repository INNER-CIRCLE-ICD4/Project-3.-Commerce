package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.infra.entity.BrandJpaEntity;
import innercircle.commerce.product.infra.entity.CategoryJpaEntity;
import innercircle.commerce.product.infra.entity.ProductJpaEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ProductRepository JPA 구현체
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final BrandJpaRepository brandJpaRepository;

    @Override
    public Product save(Product product) {
        CategoryJpaEntity category = categoryJpaRepository.findById(product.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + product.getCategoryId()));
        BrandJpaEntity brand = brandJpaRepository.findById(product.getBrandId())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + product.getBrandId()));

        ProductJpaEntity entity = ProductJpaEntity.from(product, category, brand);
        ProductJpaEntity savedEntity = productJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public boolean existsByName(String name) {
        return productJpaRepository.existsByName(name);
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findById(productId)
                .map(ProductJpaEntity::toDomain);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long excludeProductId) {
        return productJpaRepository.existsByNameAndIdNot(name, excludeProductId);
    }
}
