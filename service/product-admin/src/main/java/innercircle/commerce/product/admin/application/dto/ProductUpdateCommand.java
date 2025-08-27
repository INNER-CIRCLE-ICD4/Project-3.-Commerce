package innercircle.commerce.product.admin.application.dto;

import innercircle.commerce.product.core.domain.ProductImage;

import java.util.List;

public record ProductUpdateCommand(
        Long productId,
        String name,
        Integer basePrice,
        String detailContent,
        List<String> imagesToDelete,
        List<ProductImage> imagesToAdd
) {
}
