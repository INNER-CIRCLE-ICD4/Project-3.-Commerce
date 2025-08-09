package innercircle.commerce.product.admin.application.dto;

import java.util.List;

/**
 * 이미지 업로드 응답
 */
public record ImageUploadResponse(
		String uploadId,
		List<UploadedImage> images
) {
	public record UploadedImage(
			String tempId,
			String originalName,
			String url,
			long size,
			String contentType,
			boolean isMain,
			int sortOrder
	) {
	}
}