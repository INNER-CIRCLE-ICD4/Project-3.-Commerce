package innercircle.commerce.order.infra.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderItemEntity
 * 주문 상품 정보 엔티티 (order_items 테이블)
 */
@Entity
@Table(
    name = "order_item",
    schema = "orders",
    indexes = {
        @Index(name = "idx_order_items_order_id", columnList = "order_id"),
        @Index(name = "idx_order_items_product_id", columnList = "product_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemEntity {

    @Id
    private Long id; // 도메인에서 생성(IdGenerator)

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;
    @Column(name = "product_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal productPrice;
    @Column(name = "product_option_id", nullable = false)
    private Long productOptionId;
    @Column(name = "product_option_name", nullable = false, length = 255)
    private String productOptionName;
    @Column(name = "product_discount_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal productDiscountPrice;
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice; // (unit - discount) * qty

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public OrderItemEntity(Long id, OrderEntity order, Long productId, String productName,
                           BigDecimal productPrice, Long productOptionId, String productOptionName,
                           BigDecimal productDiscountPrice, Integer quantity, BigDecimal totalPrice,
                           String status) {
        this.id = id;
        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productOptionId = productOptionId;
        this.productOptionName = productOptionName;
        this.productDiscountPrice = productDiscountPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
