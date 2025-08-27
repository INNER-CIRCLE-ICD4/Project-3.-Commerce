package innercircle.commerce.product.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 임시 이미지 요청 (ID가 없는 새로운 이미지)
 */
public record TempImageRequest(
		@NotBlank(message = "이미지 URL은 필수입니다")
		String url,
		
		@NotBlank(message = "원본 파일명은 필수입니다") 
		String originalName,
		
		@NotNull(message = "정렬 순서는 필수입니다")
		Integer sortOrder
) {
}