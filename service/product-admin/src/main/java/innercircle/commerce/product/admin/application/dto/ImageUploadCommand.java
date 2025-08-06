package innercircle.commerce.product.admin.application.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 이미지 업로드 명령
 */
public record ImageUploadCommand(
        List<MultipartFile> files,
        List<ImageMetadata> metadata
) {
    /**
     * 이미지 메타데이터
     */
    public record ImageMetadata(
            boolean isMain,
            int sortOrder
    ) {
        public ImageMetadata {
            if (sortOrder < 1) {
                throw new IllegalArgumentException("정렬 순서는 1 이상이어야 합니다");
            }
        }
    }

    public ImageUploadCommand {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지 파일이 필요합니다");
        }

        if (metadata == null || metadata.size() != files.size()) {
            throw new IllegalArgumentException("파일 수와 메타데이터 수가 일치해야 합니다");
        }

        long mainImageCount = metadata.stream()
                .filter(ImageMetadata::isMain)
                .count();

        if (mainImageCount != 1) {
            throw new IllegalArgumentException("대표 이미지가 반드시 1개 필요합니다");
        }
    }
}