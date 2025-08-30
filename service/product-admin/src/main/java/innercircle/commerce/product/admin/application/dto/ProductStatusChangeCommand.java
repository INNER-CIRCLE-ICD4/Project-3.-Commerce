package innercircle.commerce.product.admin.application.dto;

import innercircle.commerce.product.core.domain.ProductStatus;

public record ProductStatusChangeCommand(
        Long productId,
        ProductStatus status
) {
}
