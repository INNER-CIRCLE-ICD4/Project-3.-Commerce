package innercircle.commerce.product.admin.application.dto;

import innercircle.commerce.product.core.domain.entity.Product;
import innercircle.commerce.product.core.domain.entity.ProductOption;

import java.util.Collections;
import java.util.List;

public record ProductCreateCommand(
        String name,
        Long leafCategoryId,
        Long brandId,
        Integer basePrice,
        Integer stock,
        String detailContent,
        List<ProductOption> options,
        ImageUploadCommand imageCommand
) {
    /**
     * Command 생성 시 검증
     */
    public ProductCreateCommand {
        if (imageCommand == null || imageCommand.files().isEmpty()) {
            throw new IllegalArgumentException("상품 이미지는 필수입니다");
        }
    }

    /**
     * Command를 도메인 객체로 변환합니다 (이미지 없이).
     * 이미지는 별도 프로세스에서 처리 후 업데이트됩니다.
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
                options != null ? options : Collections.emptyList(),
                Collections.emptyList(), // 이미지는 나중에 업데이트
                detailContent
        );
    }
}
