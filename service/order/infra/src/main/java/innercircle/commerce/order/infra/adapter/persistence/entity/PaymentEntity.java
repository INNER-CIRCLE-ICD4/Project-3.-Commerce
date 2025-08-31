package innercircle.commerce.order.infra.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderPaymentEntity
 * 주문 결제 정보 엔티티 (order_payment 테이블)
 */
@Entity
@Table(name = "order_payment", schema = "commerce")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;
    @Column(name = "payment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paymentAmount;
    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus;
    @Column(name = "transaction_id", length = 100)
    private String transactionId;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    PaymentEntity(Long id, OrderEntity order, String paymentMethod, BigDecimal paymentAmount,
                  String paymentStatus, String transactionId, LocalDateTime paidAt) {
        this.id = id;
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
        this.paidAt = paidAt;
        this.createdAt = LocalDateTime.now();
    }

    public void setOrder(OrderEntity order) { 
        this.order = order; 
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
