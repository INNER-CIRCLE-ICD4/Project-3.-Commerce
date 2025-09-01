package innercircle.commerce.order.application.port.out;

import innercircle.commerce.order.domain.model.vo.ProductId;

/**
 * ProductService Interface
 * 상품 서비스와의 통신을 위한 출력 포트
 */
public interface ProductService {
    /**
     * 상품 재고 가용성 확인
     *
     * @param productId 상품 ID
     * @param quantity 요청 수량
     * @return 재고 가용 여부
     */
    boolean checkAvailability(ProductId productId, int quantity);

    /**
     * 상품 재고 예약
     *
     * @param productId 상품 ID
     * @param quantity 예약할 수량
     */
    void reserveStock(ProductId productId, int quantity);

    /**
     * 예약된 재고를 해제
     *
     * @param productId 상품 ID
     * @param quantity 해제할 수량
     */
    void releaseStock(ProductId productId, int quantity);

    /**
     * 상품 정보 조회
     *
     * @param productId 상품 ID
     * @return 상품 정보
     */
    ProductInfo getProductInfo(ProductId productId);
    
    /**
     * 상품 옵션 정보 조회
     *
     * @param productId 상품 ID
     * @param productOptionId 상품 옵션 ID
     * @return 상품 옵션 정보
     */
    ProductOptionInfo getProductOptionInfo(ProductId productId, Long productOptionId);

    /**
     * 상품 정보 DTO
     */
    class ProductInfo {
        private final Long id;
        private final String name;
        private final String description;
        private final Money price;
        private final int stockQuantity;
        private final boolean available;

        public ProductInfo(Long id, String name, String description,
                         Money price, int stockQuantity, boolean available) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.stockQuantity = stockQuantity;
            this.available = available;
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Money getPrice() { return price; }
        public int getStockQuantity() { return stockQuantity; }
        public boolean isAvailable() { return available; }
    }
    
    /**
     * 상품 옵션 정보 DTO
     */
    class ProductOptionInfo {
        private final Long id;
        private final String name;
        private final Money price;
        private final Money discountPrice;
        private final int stockQuantity;
        private final boolean available;

        public ProductOptionInfo(Long id, String name, Money price, Money discountPrice, 
                               int stockQuantity, boolean available) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.discountPrice = discountPrice;
            this.stockQuantity = stockQuantity;
            this.available = available;
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public Money getPrice() { return price; }
        public Money getDiscountPrice() { return discountPrice; }
        public int getStockQuantity() { return stockQuantity; }
        public boolean isAvailable() { return available; }
    }

    class Money {
        private final java.math.BigDecimal value;

        public Money(java.math.BigDecimal value) {
            this.value = value;
        }

        public java.math.BigDecimal getValue() {
            return value;
        }
    }
}
