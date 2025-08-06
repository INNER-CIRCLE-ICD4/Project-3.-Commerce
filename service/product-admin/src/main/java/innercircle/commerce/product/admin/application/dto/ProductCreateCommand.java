package innercircle.commerce.product.admin.application.dto;

import innercircle.commerce.product.core.domain.entity.Product;
import innercircle.commerce.product.core.domain.entity.ProductImage;
import innercircle.commerce.product.core.domain.entity.ProductOption;

import java.util.List;

public record ProductCreateCommand(
        String name,
        Long leafCategoryId,
        Long brandId,
        Integer basePrice,
        Integer stock,
        List<ProductImage> images,
        String detailContent,
        List<ProductOption> options
) {
    /**
     * Command를 도메인 객체로 변환합니다.
     * 통합된 create() 메서드를 사용합니다.
     * 
     * @return 생성된 Product 도메인 객체
     */
    public Product toDomain() {
        return Product.create(
                name,
                leafCategoryId,
                brandId,
                basePrice,
                stock,
                options,  // 옵션은 null이나 빈 리스트여도 처리됨
                images,
                detailContent
        );
    }
}
