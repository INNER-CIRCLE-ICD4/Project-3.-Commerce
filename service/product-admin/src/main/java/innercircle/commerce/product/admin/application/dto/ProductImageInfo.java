package innercircle.commerce.product.admin.application.dto;

public record ProductImageInfo(
		Long id,
		String originalName,
		String url,
		boolean isMain,
		Integer sortOrder
) {
}
