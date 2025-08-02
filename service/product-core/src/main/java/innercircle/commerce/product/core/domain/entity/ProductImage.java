package innercircle.commerce.product.core.domain.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductImage {
    private Long id;
    private Long productId;
    private String url;
    private String originalName;
    private boolean isMain;
    private int sortOrder;
    
    /**
     * 상품 이미지 리스트의 유효성을 검증합니다.
     * - 이미지가 최소 1개 이상 존재해야 함
     * - 대표 이미지가 정확히 1개 존재해야 함
     *
     * @param images 검증할 상품 이미지 리스트
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static void validateImages(java.util.List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("상품 이미지는 최소 1개 이상 필요합니다");
        }
        
        long mainImageCount = images.stream()
            .filter(ProductImage::isMain)
            .count();
            
        if (mainImageCount != 1) {
            throw new IllegalArgumentException("대표 이미지가 반드시 1개 필요합니다");
        }
    }
}
