package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.infra.entity.ProductJpaEntity;
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

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = ProductJpaEntity.from(product);
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