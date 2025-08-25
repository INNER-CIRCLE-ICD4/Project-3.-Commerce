package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductImage;

import java.time.LocalDateTime;
import java.util.List;

public record ProductImageUpdateResponse(
		Long id,
		String name,
		List<ImageInfo> images,
		LocalDateTime updatedAt
) {
	public static ProductImageUpdateResponse from(Product product) {
		List<ImageInfo> imageInfos = product.getImages() != null 
				? product.getImages().stream()
						.map(ImageInfo::from)
						.toList()
				: List.of();
				
		return new ProductImageUpdateResponse(
				product.getId(),
				product.getName(),
				imageInfos,
				product.getUpdatedAt()
		);
	}
	
	public record ImageInfo(
			Long id,
			String url,
			String originalName,
			boolean isMain,
			int sortOrder
	) {
		public static ImageInfo from(ProductImage image) {
			return new ImageInfo(
					image.getId(),
					image.getUrl(),
					image.getOriginalName(),
					image.isMain(),
					image.getSortOrder()
			);
		}
	}
}