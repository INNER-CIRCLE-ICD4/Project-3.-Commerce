package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;

import java.time.LocalDateTime;

public record ProductUpdateResponse(
		Long id,
		String name,
		Long categoryId,
		Long brandId,
		Integer price,
		ProductStatus status,
		SaleType saleType,
		Integer stock,
		String detailContent,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	public static ProductUpdateResponse from(Product product) {
		return new ProductUpdateResponse(
				product.getId(),
				product.getName(),
				product.getCategoryId(),
				product.getBrandId(),
				product.getPrice(),
				product.getStatus(),
				product.getSaleType(),
				product.getStock(),
				product.getDetailContent(),
				product.getCreatedAt(),
				product.getUpdatedAt()
		);
	}
}