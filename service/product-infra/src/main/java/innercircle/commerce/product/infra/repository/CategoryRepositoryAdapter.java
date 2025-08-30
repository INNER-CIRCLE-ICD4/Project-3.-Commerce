package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.core.application.repository.CategoryRepository;
import innercircle.commerce.product.core.domain.Category;
import innercircle.commerce.product.infra.entity.CategoryJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Category save(Category category) {
        CategoryJpaEntity entity = CategoryJpaEntity.from(category);
        CategoryJpaEntity savedEntity = categoryJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Category> findById(Long categoryId) {
        return categoryJpaRepository.findById(categoryId)
                .map(CategoryJpaEntity::toDomain);
    }

    @Override
    public boolean existsById(Long categoryId) {
        return categoryJpaRepository.existsById(categoryId);
    }
}
