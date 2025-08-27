package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.core.domain.ProductImage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 상품에 새로운 이미지 추가 요청
 */
public record ProductImageAddRequest(
		@NotEmpty(message = "추가할 이미지는 최소 1개 이상 필요합니다")
		@Valid
		List<TempImageRequest> images
) {
	public List<ProductImage> toProductImages(Long productId) {
		return images.stream()
				.map(imageRequest -> ProductImage.create(
						productId,
						imageRequest.url(),
						imageRequest.originalName(),
						imageRequest.sortOrder()
				))
				.toList();
	}
}