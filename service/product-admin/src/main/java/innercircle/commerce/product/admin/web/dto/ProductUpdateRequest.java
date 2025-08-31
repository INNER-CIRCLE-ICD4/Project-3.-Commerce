package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.admin.application.dto.ProductUpdateCommand;
import innercircle.commerce.product.core.domain.ProductImage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Collections;
import java.util.List;

public record ProductUpdateRequest(
		@NotBlank(message = "상품명은 필수입니다")
		String name,
		
		@NotNull(message = "가격은 필수입니다")
		@Positive(message = "가격은 양수여야 합니다")
		Integer basePrice,
		
		@NotBlank(message = "상품 상세 내용은 필수입니다")
		String detailContent,
		
		List<String> imagesToDelete,
		List<ProductImageRequest> imagesToAdd
) {
	public ProductUpdateCommand toCommand(Long productId) {
		List<ProductImage> productImages = imagesToAdd != null ? 
			imagesToAdd.stream()
				.map(request -> request.toProductImage(productId))
				.toList() : 
			Collections.emptyList();
				
		return new ProductUpdateCommand(
				productId,
				name,
				basePrice,
				detailContent,
				imagesToDelete != null ? imagesToDelete : Collections.emptyList(),
				productImages
		);
	}
}