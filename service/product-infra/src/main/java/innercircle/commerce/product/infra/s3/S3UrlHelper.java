package innercircle.commerce.product.infra.s3;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * S3 URL 처리를 담당하는 헬퍼 클래스
 * S3 URL 파싱, 키 추출, 경로 생성 등의 책임을 담당
 */
@Component
public class S3UrlHelper {

    private static final String AMAZON_DOMAIN = ".com/";
    private static final String AMAZON_AWS_DOMAIN = "amazonaws.com";
    private static final String PRODUCT_PATH_FORMAT = "commerce/products/%d/%d.%s";

    /**
     * S3 URL에서 키를 추출합니다.
     * 
     * @param url S3 URL
     * @return S3 키
     * @throws IllegalArgumentException 유효하지 않은 URL인 경우
     */
    public String extractKeyFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("URL이 비어있습니다.");
        }

        if (!url.contains(AMAZON_DOMAIN) || !url.contains(AMAZON_AWS_DOMAIN)) {
            throw new IllegalArgumentException("유효하지 않은 S3 URL입니다: " + url);
        }

        int keyStartIndex = url.indexOf(AMAZON_DOMAIN) + AMAZON_DOMAIN.length();
        return url.substring(keyStartIndex);
    }

    /**
     * S3 URL에서 키를 추출합니다. (복잡한 파싱 방식)
     * 
     * @param url S3 URL
     * @return S3 키
     */
    public String extractKeyFromUrlComplex(String url) {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("URL이 비어있습니다.");
        }

        String[] parts = url.split("/");
        StringBuilder keyBuilder = new StringBuilder();
        boolean foundAmazonaws = false;

        for (String part : parts) {
            if (foundAmazonaws) {
                if (!keyBuilder.isEmpty()) {
                    keyBuilder.append("/");
                }
                keyBuilder.append(part);
            } else if (part.contains(AMAZON_AWS_DOMAIN)) {
                foundAmazonaws = true;
            }
        }

        if (keyBuilder.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 S3 URL입니다: " + url);
        }

        return keyBuilder.toString();
    }

    /**
     * 상품 이미지의 최종 경로를 생성합니다.
     * 
     * @param productId 상품 ID
     * @param imageId 이미지 ID
     * @param extension 파일 확장자 (점 제외)
     * @return 상품 이미지 경로
     */
    public String buildProductImageKey(Long productId, Long imageId, String extension) {
        if (productId == null || imageId == null) {
            throw new IllegalArgumentException("상품 ID와 이미지 ID는 필수입니다.");
        }
        
        if (!StringUtils.hasText(extension)) {
            extension = "jpg";
        }

        return String.format(PRODUCT_PATH_FORMAT, productId, imageId, extension.toLowerCase());
    }

    /**
     * 파일명에서 확장자를 추출합니다.
     * 
     * @param filename 파일명
     * @return 확장자 (점 제외), 없으면 "jpg"
     */
    public String extractExtensionFromFilename(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "jpg";
        }

        String[] parts = filename.split("\\.");
        String extension = parts[parts.length - 1].toLowerCase();
        
        // URL 쿼리 파라미터 제거
        if (extension.contains("?")) {
            extension = extension.split("\\?")[0];
        }
        
        return extension;
    }

    /**
     * URL 또는 원본 파일명에서 확장자를 추출합니다.
     * 
     * @param url URL
     * @param originalName 원본 파일명
     * @return 확장자 (점 제외)
     */
    public String extractExtensionFromUrlOrName(String url, String originalName) {
        if (StringUtils.hasText(url) && url.contains(".")) {

            String[] urlParts = url.split("/");
            String filename = urlParts[urlParts.length - 1];
            
            if (filename.contains(".")) {
                String extension = extractExtensionFromFilename(filename);
                if (!"jpg".equals(extension)) { // 기본값이 아닌 경우만 반환
                    return extension;
                }
            }
        }

        return extractExtensionFromFilename(originalName);
    }
}