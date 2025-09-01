package innercircle.commerce.product.api.application.exception;

/**
 * 상품 이미지 업로드가 진행 중일 때 발생하는 예외
 */
public class ProductImageUploadInProgressException extends RuntimeException {
    
    private final Long productId;
    
    public ProductImageUploadInProgressException(Long productId) {
        this.productId = productId;
    }
    
    public Long getProductId() {
        return productId;
    }
}
