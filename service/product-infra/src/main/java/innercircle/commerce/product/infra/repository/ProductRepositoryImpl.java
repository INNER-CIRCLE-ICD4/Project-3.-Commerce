package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.infra.entity.BrandJpaEntity;
import innercircle.commerce.product.infra.entity.CategoryJpaEntity;
import innercircle.commerce.product.infra.entity.ProductJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Product save(Product product) {
        CategoryJpaEntity category = categoryJpaRepository.findById(product.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + product.getCategoryId()));
        BrandJpaEntity brand = brandJpaRepository.findById(product.getBrandId())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + product.getBrandId()));

        ProductJpaEntity entity;
        if (product.getId() != null) {
            // 기존 엔티티 업데이트
            entity = productJpaRepository.findById(product.getId())
                    .orElse(ProductJpaEntity.from(product, category, brand));
            // 기존 엔티티의 값들을 업데이트
            entity.updateFrom(product, category, brand);
        } else {
            // 새로운 엔티티 생성
            entity = ProductJpaEntity.from(product, category, brand);
        }
        
        ProductJpaEntity savedEntity = productJpaRepository.save(entity);
        entityManager.flush(); // 명시적으로 flush하여 @Version 업데이트 강제 수행
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

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productJpaRepository.findAll(pageable)
                .map(ProductJpaEntity::toDomain);
    }

    @Override
    public Page<Product> findAllByStatus(ProductStatus status, Pageable pageable) {
        return productJpaRepository.findAllByStatus(status, pageable)
                .map(ProductJpaEntity::toDomain);
    }

    @Override
    public Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable) {
        return productJpaRepository.findAllByCategoryId(categoryId, pageable)
                .map(ProductJpaEntity::toDomain);
    }

    @Override
    public Page<Product> findAllByStatusAndCategoryId(ProductStatus status, Long categoryId, Pageable pageable) {
        return productJpaRepository.findAllByStatusAndCategoryId(status, categoryId, pageable)
                .map(ProductJpaEntity::toDomain);
    }

    @Override
    public Page<Product> findProducts(ProductStatus status, Long categoryId, Pageable pageable) {
        if (status != null && categoryId != null) {
            return productJpaRepository.findAllByStatusAndCategoryId(status, categoryId, pageable)
                    .map(ProductJpaEntity::toDomain);
        } else if (status != null) {
            return productJpaRepository.findAllByStatus(status, pageable)
                    .map(ProductJpaEntity::toDomain);
        } else if (categoryId != null) {
            return productJpaRepository.findAllByCategoryId(categoryId, pageable)
                    .map(ProductJpaEntity::toDomain);
        } else {
            return productJpaRepository.findAll(pageable)
                    .map(ProductJpaEntity::toDomain);
        }
    }
}
