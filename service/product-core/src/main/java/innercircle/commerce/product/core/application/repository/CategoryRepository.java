package innercircle.commerce.product.core.application.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository {
    /**
     * 카테고리 ID로 카테고리 존재 여부를 확인합니다.
     * 
     * @param categoryId 카테고리 ID
     * @return 존재 여부
     */
    boolean existsById(Long categoryId);
}
