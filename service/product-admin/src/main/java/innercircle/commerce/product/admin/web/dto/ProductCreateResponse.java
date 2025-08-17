package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;

import java.time.LocalDateTime;

public record ProductCreateResponse(
		Long id,
		String name,
		Long categoryId,
		Long brandId,
		Integer price,
		ProductStatus status,
		SaleType saleType,
		Integer stock,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	public static ProductCreateResponse from (Product product) {
		return new ProductCreateResponse(
				product.getId(),
				product.getName(),
				product.getCategoryId(),
				product.getBrandId(),
				product.getPrice(),
				product.getStatus(),
				product.getSaleType(),
				product.getStock(),
				product.getCreatedAt(),
				product.getUpdatedAt()
		);
	}
}