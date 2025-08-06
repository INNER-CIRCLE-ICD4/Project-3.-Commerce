package innercircle.commerce.product.admin.application.dto;

import innercircle.commerce.product.core.domain.entity.ProductImage;

import java.util.List;

public record ProductImageUpdateCommand(
        Long productId,
        List<ProductImage> images
) {
}
