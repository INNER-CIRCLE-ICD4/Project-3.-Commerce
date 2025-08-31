package innercircle.commerce.order.infra.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ShippingStatusHistoryEntity
 * 배송 상태 이력 엔티티 (shipping_status_history 테이블)
 */
@Entity
@Table(name = "shipping_status_history", schema = "commerce")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingStatusHistoryEntity {
    
    @Id
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipping_id", nullable = false)
    private ShippingEntity shipping;
    
    @Column(name = "status", nullable = false, length = 50)
    private String status;
    
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    ShippingStatusHistoryEntity(Long id, ShippingEntity shipping, String status, String note) {
        this.id = id;
        this.shipping = shipping;
        this.status = status;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
