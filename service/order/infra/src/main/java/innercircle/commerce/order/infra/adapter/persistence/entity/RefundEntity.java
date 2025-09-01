package innercircle.commerce.order.infra.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderRefundEntity  
 * 주문 환불/반품 정보 엔티티 (order_refund 테이블)
 */
@Entity
@Table(name = "order_refund", schema = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundEntity {
    
    @Id
    @Column(name = "id")
    private Long id;
    
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId; // order_item 테이블의 id와 연결
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "refund_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "refund_status", nullable = false, length = 20)
    private String refundStatus;
    
    @Column(name = "requested_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime requestedAt;
    
    @Column(name = "refunded_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime refundedAt;
    
    RefundEntity(Long id, Long orderItemId, String reason, BigDecimal refundAmount,
                 String refundStatus, LocalDateTime requestedAt, LocalDateTime refundedAt) {
        this.id = id;
        this.orderItemId = orderItemId;
        this.reason = reason;
        this.refundAmount = refundAmount;
        this.refundStatus = refundStatus;
        this.requestedAt = requestedAt;
        this.refundedAt = refundedAt;
    }
}
