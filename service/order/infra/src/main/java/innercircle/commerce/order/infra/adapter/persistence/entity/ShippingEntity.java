package innercircle.commerce.order.infra.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ShippingEntity
 * 배송 정보 엔티티 (shipping 테이블)
 */
@Entity
@Table(name = "shipping", schema = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingEntity {

    @Id
    @Column(name = "id")
    private Long id;
    
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId; // order_item 테이블의 id와 연결
    
    @Column(name = "courier", length = 100)
    private String courier;
    
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;
    
    @Column(name = "shipped_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime shippedAt;
    
    @Column(name = "delivered_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime deliveredAt;
    
    @Column(name = "current_status", length = 50)
    private String currentStatus;
    
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    ShippingEntity(Long id, Long orderItemId, String courier, String trackingNumber,
                   LocalDateTime shippedAt, LocalDateTime deliveredAt, String currentStatus) {
        this.id = id;
        this.orderItemId = orderItemId;
        this.courier = courier;
        this.trackingNumber = trackingNumber;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
        this.currentStatus = currentStatus;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
