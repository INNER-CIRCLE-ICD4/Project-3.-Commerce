package innercircle.commerce.order.infra.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderEntity
 * 주문 기본 정보 엔티티 (order 테이블)
 */
@Entity
@Table(name = "order", schema = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity {
    @Id
    private Long id; // 도메인에서 생성(IdGenerator) → Assigned

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "order_number", nullable = false, length = 100)
    private String orderNumber;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    // 배송지
    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;
    @Column(name = "recipient_phone", nullable = false, length = 30)
    private String recipientPhone;
    @Column(name = "address_code", nullable = false, length = 20)
    private String addressCode;
    @Column(name = "address", nullable = false, length = 255)
    private String address;
    @Column(name = "address_detail", nullable = false, length = 255)
    private String addressDetail;

    // 금액 집계 (총액/총할인/결제금액)
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalAmount; // (할인전 총합)
    @Column(name = "total_discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;
    @Column(name = "pay_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentEntity> payments = new ArrayList<>();

    public OrderEntity(Long id, Long memberId, String orderNumber, LocalDateTime orderDate,
                       String recipientName, String recipientPhone, String addressCode,
                       String address, String addressDetail, BigDecimal originalAmount,
                       BigDecimal discountAmount, BigDecimal finalAmount, String status) {
        this.id = id;
        this.memberId = memberId;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.addressCode = addressCode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.originalAmount = originalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    /* Helpers */
    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    public void addPayment(PaymentEntity payment) {
        payments.add(payment);
        payment.setOrder(this);
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
