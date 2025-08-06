package innercircle.commerce.product.admin.application.dto;

import innercircle.commerce.product.core.domain.entity.SaleType;

public record ProductSaleTypeChangeCommand(
        Long productId,
        SaleType saleType
) {
}
