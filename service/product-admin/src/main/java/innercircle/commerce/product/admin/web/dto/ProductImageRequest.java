package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.admin.application.dto.ProductImageInfo;
import innercircle.commerce.product.core.domain.ProductImage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductImageRequest(
		@NotNull(message = "이미지 ID는 필수입니다")
		Long id,
		
		@NotBlank(message = "원본 파일명은 필수입니다") 
		String originalName,
		
		@NotBlank(message = "이미지 URL은 필수입니다")
		String url,
		
		@NotNull(message = "정렬 순서는 필수입니다")
		Integer sortOrder
) {
	public ProductImageInfo toImageInfo() {
		return new ProductImageInfo(
				id,
				originalName,
				url,
				sortOrder
		);
	}
	
	public ProductImage toProductImage(Long productId) {
		return ProductImage.create(
				productId,
				url,
				originalName,
				sortOrder
		);
	}
}