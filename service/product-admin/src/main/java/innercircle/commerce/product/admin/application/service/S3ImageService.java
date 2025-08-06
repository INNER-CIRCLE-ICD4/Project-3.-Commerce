package innercircle.commerce.product.admin.application.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * S3를 이용한 이미지 업로드/삭제 서비스
 */
@Service
public class S3ImageService {

    private final AmazonS3Client amazonS3Client;
    private final String bucketName;
    private final String baseUrl;

    public S3ImageService(
            AmazonS3Client amazonS3Client,
            @Value("${cloud.aws.s3.bucket}") String bucketName,
            @Value("${cloud.aws.s3.base-url}") String baseUrl
    ) {
        this.amazonS3Client = amazonS3Client;
        this.bucketName = bucketName;
        this.baseUrl = baseUrl;
    }

    /**
     * 임시 경로에 이미지를 업로드합니다.
     * 
     * @param file 업로드할 파일
     * @return 업로드된 이미지 정보
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    public UploadedImageInfo uploadToTemp(MultipartFile file) throws IOException {
        String tempId = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();
        String extension = getFileExtension(originalName);
        String s3Key = buildTempPath(tempId, extension);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        metadata.addUserMetadata("original-name", originalName);

        PutObjectRequest request = new PutObjectRequest(
                bucketName, s3Key, file.getInputStream(), metadata);

        amazonS3Client.putObject(request);

        String url = baseUrl + "/" + s3Key;

        return new UploadedImageInfo(
                tempId,
                originalName,
                url,
                s3Key,
                file.getSize(),
                file.getContentType()
        );
    }

    /**
     * 임시 이미지를 정식 경로로 이동합니다.
     * 
     * @param tempId 임시 이미지 ID
     * @param productId 상품 ID
     * @param imageId 이미지 ID
     * @return 정식 경로의 URL
     */
    public String moveToProduct(String tempId, Long productId, Long imageId) {
        String tempKey = findTempKey(tempId);
        String extension = getExtensionFromKey(tempKey);
        String productKey = buildProductPath(productId, imageId, extension);

        // 복사
        amazonS3Client.copyObject(bucketName, tempKey, bucketName, productKey);
        
        // 원본 삭제
        amazonS3Client.deleteObject(bucketName, tempKey);

        return baseUrl + "/" + productKey;
    }

    /**
     * 임시 파일들을 일괄 삭제합니다.
     * 
     * @param tempIds 삭제할 임시 이미지 ID 목록
     */
    public void deleteTempImages(List<String> tempIds) {
        for (String tempId : tempIds) {
            try {
                String tempKey = findTempKey(tempId);
                amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, tempKey));
            } catch (Exception e) {
                // 로깅은 하되 예외는 전파하지 않음 (정리 작업이므로)
                // TODO: 로거 추가
                System.err.println("임시 파일 삭제 실패: " + tempId + ", " + e.getMessage());
            }
        }
    }

    /**
     * 상품 이미지들을 일괄 삭제합니다.
     * 
     * @param productId 상품 ID
     * @param imageIds 삭제할 이미지 ID 목록
     */
    public void deleteProductImages(Long productId, List<Long> imageIds) {
        for (Long imageId : imageIds) {
            try {
                String productKey = findProductKey(productId, imageId);
                amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, productKey));
            } catch (Exception e) {
                // 로깅은 하되 예외는 전파하지 않음
                System.err.println("상품 이미지 삭제 실패: " + productId + "/" + imageId + ", " + e.getMessage());
            }
        }
    }

    /**
     * 임시 경로 생성
     */
    private String buildTempPath(String tempId, String extension) {
        return String.format("commerce/temp/images/%s/original.%s", tempId, extension);
    }

    /**
     * 상품 이미지 경로 생성
     */
    private String buildProductPath(Long productId, Long imageId, String extension) {
        return String.format("commerce/products/%d/%d.%s", productId, imageId, extension);
    }

    /**
     * 임시 파일 키 찾기 (실제로는 패턴 매칭으로 찾아야 함)
     */
    private String findTempKey(String tempId) {
        // 실제 구현에서는 S3 목록 조회 또는 별도 관리가 필요
        // 여기서는 간단히 가능한 확장자들을 시도
        String[] extensions = {"jpg", "jpeg", "png", "webp"};
        
        for (String ext : extensions) {
            String key = buildTempPath(tempId, ext);
            if (amazonS3Client.doesObjectExist(bucketName, key)) {
                return key;
            }
        }
        
        throw new RuntimeException("임시 파일을 찾을 수 없습니다: " + tempId);
    }

    /**
     * 상품 이미지 키 찾기
     */
    private String findProductKey(Long productId, Long imageId) {
        String[] extensions = {"jpg", "jpeg", "png", "webp"};
        
        for (String ext : extensions) {
            String key = buildProductPath(productId, imageId, ext);
            if (amazonS3Client.doesObjectExist(bucketName, key)) {
                return key;
            }
        }
        
        throw new RuntimeException("상품 이미지를 찾을 수 없습니다: " + productId + "/" + imageId);
    }

    /**
     * 파일명에서 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "jpg";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) return "jpg";
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * S3 키에서 확장자 추출
     */
    private String getExtensionFromKey(String key) {
        int lastDotIndex = key.lastIndexOf('.');
        if (lastDotIndex == -1) return "jpg";
        return key.substring(lastDotIndex + 1);
    }

    /**
     * 업로드된 이미지 정보
     */
    public record UploadedImageInfo(
            String tempId,
            String originalName,
            String url,
            String s3Key,
            long size,
            String contentType
    ) {
    }
}