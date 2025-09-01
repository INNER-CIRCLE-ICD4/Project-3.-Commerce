package innercircle.commerce.product.admin.application.dto;

import innercircle.commerce.product.core.domain.ProductImage;

public record ProductImageInfo(
		Long id,
		String originalName,
		String url,
		Integer sortOrder
) {
	public static ProductImageInfo from(ProductImage productImage) {
		return new ProductImageInfo(
				productImage.getId(),
				productImage.getOriginalName(),
				productImage.getUrl(),
				productImage.getSortOrder()
		);
	}
}
