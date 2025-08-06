package innercircle.commerce.product.admin.application.dto;

import java.util.List;

/**
 * 이미지 업로드 응답
 */
public record ImageUploadResponse(
        String uploadId,  // 배치 업로드 ID
        List<UploadedImage> images
) {
    /**
     * 업로드된 이미지 정보
     */
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