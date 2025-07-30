package innercircle.commerce.product.core.domain.entity;

/**
 * 상품 상태를 나타내는 열거형
 */
public enum ProductStatus {
    /**
     * 활성 상태 - 판매 가능한 상품
     */
    ACTIVE,
    
    /**
     * 비활성 상태 - 일시적으로 판매하지 않는 상품
     */
    INACTIVE,
    
    /**
     * 품절 상태 - 재고가 없어 판매할 수 없는 상품
     */
    SOLD_OUT
}
