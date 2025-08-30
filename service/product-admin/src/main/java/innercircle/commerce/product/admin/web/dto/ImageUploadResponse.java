package innercircle.commerce.product.admin.web.dto;


import innercircle.commerce.product.admin.application.dto.ImageUploadInfo;

/**
 * 이미지 업로드 응답 DTO
 */
public record ImageUploadResponse(
		Long id,
		String originalName,
		String url
) {
	public static ImageUploadResponse from (ImageUploadInfo uploadInfo) {
		return new ImageUploadResponse(
				uploadInfo.id(),
				uploadInfo.originalName(),
				uploadInfo.url()
		);
	}
}