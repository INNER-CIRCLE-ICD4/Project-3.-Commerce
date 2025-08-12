package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.core.domain.entity.Product;
import innercircle.commerce.product.core.domain.entity.ProductStatus;
import innercircle.commerce.product.core.domain.entity.SaleType;

import java.time.LocalDateTime;

public record ProductCreateResponse(
		Long id,
		String name,
		String code,
		Long leafCategoryId,
		Long brandId,
		Integer basePrice,
		ProductStatus status,
		SaleType saleType,
		Integer stock,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	public static ProductCreateResponse from(Product product) {
		return new ProductCreateResponse(
				product.getId(),
				product.getName(),
				product.getCode(),
				product.getLeafCategoryId(),
				product.getBrandId(),
				product.getBasePrice(),
				product.getStatus(),
				product.getSaleType(),
				product.getStock(),
				product.getCreatedAt(),
				product.getUpdatedAt()
		);
	}
}