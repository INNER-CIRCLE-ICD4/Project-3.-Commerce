package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.dto.ProductImageInfo;
import innercircle.commerce.product.core.domain.entity.ProductOption;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record ProductCreateRequest(
		@NotBlank(message = "상품명은 필수입니다")
		String name,
		
		@NotNull(message = "카테고리 ID는 필수입니다")
		@Positive(message = "카테고리 ID는 양수여야 합니다")
		Long leafCategoryId,
		
		@NotNull(message = "브랜드 ID는 필수입니다")
		@Positive(message = "브랜드 ID는 양수여야 합니다")
		Long brandId,
		
		@NotNull(message = "가격은 필수입니다")
		@Positive(message = "가격은 양수여야 합니다")
		Integer basePrice,
		
		@NotNull(message = "재고는 필수입니다")
		@PositiveOrZero(message = "재고는 0 이상이어야 합니다")
		Integer stock,
		
		@NotBlank(message = "상품 상세 내용은 필수입니다")
		String detailContent,
		
		List<ProductOption> options,
		
		@NotEmpty(message = "상품 이미지는 최소 1개 이상 필요합니다")
		List<ProductImageRequest> images
) {
	public ProductCreateCommand toCommand() {
		List<ProductImageInfo> imageInfos = images.stream()
				.map(ProductImageRequest::toImageInfo)
				.toList();
				
		return new ProductCreateCommand(
				name,
				leafCategoryId,
				brandId,
				basePrice,
				stock,
				detailContent,
				options,
				imageInfos
		);
	}
}