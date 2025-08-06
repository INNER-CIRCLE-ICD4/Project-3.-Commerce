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
     * 옵션이 있으면 createWithOptions(), 없으면 create()를 호출합니다.
     * 
     * @return 생성된 Product 도메인 객체
     */
    public Product toDomain() {
        if (options == null || options.isEmpty()) {
            return Product.create(
                    name,
                    leafCategoryId,
                    brandId,
                    basePrice,
                    stock,
                    images,
                    detailContent
            );
        } else {
            return Product.createWithOptions(
                    name,
                    leafCategoryId,
                    brandId,
                    basePrice,
                    stock,
                    images,
                    detailContent,
                    options
            );
        }
    }
}
