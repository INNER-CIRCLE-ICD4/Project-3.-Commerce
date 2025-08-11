package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.core.application.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * CategoryRepository JPA 구현체
 */
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public boolean existsById(Long categoryId) {
        return categoryJpaRepository.existsById(categoryId);
    }
}