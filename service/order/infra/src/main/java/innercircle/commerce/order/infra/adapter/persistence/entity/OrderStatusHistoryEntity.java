package innercircle.commerce.order.infra.adapter.persistence.entity;

import innercircle.commerce.order.domain.model.vo.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OrderStatusHistoryEntity
 * 주문 상태 이력 엔티티 (order_status_history 테이블)
 */
@Entity
@Table(
        name = "order_status_history",
        schema = "orders",
        indexes = {
                @Index(name = "idx_order_status_history_order_item_id", columnList = "order_item_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatusHistoryEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItemEntity orderItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderItemStatus status;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    OrderStatusHistoryEntity(Long id, OrderItemEntity orderItem, OrderItemStatus status,
                             LocalDateTime changedAt, String note) {
        this.id = id;
        this.orderItem = orderItem;
        this.status = status;
        this.changedAt = changedAt;
        this.note = note;
    }

    @PrePersist
    protected void onCreate() {
        if (this.changedAt == null) this.changedAt = LocalDateTime.now();
    }
}
