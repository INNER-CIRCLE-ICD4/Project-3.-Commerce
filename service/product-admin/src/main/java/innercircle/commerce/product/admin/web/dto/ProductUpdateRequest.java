package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.admin.application.dto.ProductUpdateCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductUpdateRequest(
		@NotBlank(message = "상품명은 필수입니다")
		String name,
		
		@NotNull(message = "가격은 필수입니다")
		@Positive(message = "가격은 양수여야 합니다")
		Integer basePrice,
		
		@NotBlank(message = "상품 상세 내용은 필수입니다")
		String detailContent
) {
	public ProductUpdateCommand toCommand(Long productId) {
		return new ProductUpdateCommand(
				productId,
				name,
				basePrice,
				detailContent
		);
	}
}