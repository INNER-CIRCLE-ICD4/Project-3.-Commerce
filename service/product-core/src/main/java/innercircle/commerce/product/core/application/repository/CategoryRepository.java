package innercircle.commerce.product.core.application.repository;

import innercircle.commerce.product.core.domain.Category;

import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(Long categoryId);
    boolean existsById(Long categoryId);
}
