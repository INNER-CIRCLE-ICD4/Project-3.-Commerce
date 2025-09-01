package innercircle.commerce.order.domain.model.aggregate;

import innercircle.commerce.order.domain.model.entity.OrderItem;
import innercircle.commerce.order.domain.model.entity.OrderPayment;
import innercircle.commerce.order.domain.model.vo.*;
import innercircle.commerce.order.domain.event.DomainEvent;
import innercircle.commerce.order.domain.event.OrderCancelledEvent;
import innercircle.commerce.order.domain.event.OrderCreatedEvent;
import innercircle.commerce.order.domain.event.OrderPaidEvent;
import innercircle.commerce.order.domain.model.vo.enums.OrderStatus;
import innercircle.commerce.order.domain.model.vo.enums.PaymentMethodType;
import innercircle.commerce.order.domain.model.vo.enums.PaymentStatus;
import innercircle.commerce.order.domain.services.IdGenerator;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Order Aggregate Root
 * 주문 집합체 루트
 */
@Getter
public class Order {
    
    private static final String CANNOT_BE_CONFIRMED_MESSAGE = "Order cannot be confirmed in status: %s";
    private static final String CANNOT_BE_CANCELLED_MESSAGE = "Order cannot be cancelled in status: %s";
    private static final String CANNOT_BE_SHIPPED_MESSAGE = "Order cannot be shipped in status: %s";
    private static final String CANNOT_BE_DELIVERED_MESSAGE = "Order cannot be delivered in status: %s";
    private static final String CANNOT_BE_COMPLETED_MESSAGE = "Order cannot be completed in status: %s";
    
    private final OrderId id;
    private final OrderNumber orderNumber;
    private final MemberId memberId; 
    private final LocalDateTime orderedAt;
    private final Money totalAmount;
    private OrderStatus status;
    private final ShippingAddress shippingAddress;
    private final List<OrderPayment> payments;
    private final List<OrderItem> orderItems;
    private final List<DomainEvent> domainEvents;
    private LocalDateTime modifiedAt;
    private String cancelReason;

    // Private Constructor - Factory 메서드를 통해서만 생성
    private Order(
            OrderId id,
            OrderNumber orderNumber,
            MemberId memberId, 
            ShippingAddress shippingAddress,
            List<OrderItem> orderItems,
            Money totalAmount,
            OrderStatus status,
            LocalDateTime orderedAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.memberId = memberId; 
        this.shippingAddress = shippingAddress;
        this.payments = new ArrayList<>();
        this.orderItems = new ArrayList<>(orderItems);
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderedAt = orderedAt;
        this.domainEvents = new ArrayList<>();
        this.modifiedAt = LocalDateTime.now();
    }

    /**
     * 새 주문 생성 - Static Factory Method
     * @param memberId 회원 ID
     * @param shippingAddress 배송 주소
     * @param orderItems 주문 항목 리스트
     * @param idGenerator ID 생성기
     * @return 생성된 주문
     */
    public static Order createNew(
            MemberId memberId, 
            ShippingAddress shippingAddress, 
            List<OrderItem> orderItems,
            IdGenerator idGenerator) {
        
        validateOrderItems(orderItems);
        
        Order order = new Order(
                OrderId.of(idGenerator.generateId()),
                OrderNumber.generate(),
                memberId, 
                shippingAddress,
                orderItems,
                calculateTotalAmount(orderItems),
                OrderStatus.PENDING,
                LocalDateTime.now()
        );
        
        order.raiseOrderCreatedEvent();
        
        return order;
    }
    
    /**
     * 기존 주문 복원 - Repository에서 사용
     */
    public static Order restore(
            OrderId id,
            OrderNumber orderNumber,
            MemberId memberId, 
            ShippingAddress shippingAddress,
            List<OrderItem> orderItems,
            Money totalAmount,
            OrderStatus status,
            LocalDateTime orderedAt,
            LocalDateTime modifiedAt,
            List<OrderPayment> payments,
            String cancelReason) {

        Order order = new Order(
                id,
                orderNumber,
                memberId,
                shippingAddress,
                orderItems,
                totalAmount,
                status,
                orderedAt
        );
        
        order.modifiedAt = modifiedAt;
        if (payments != null) {
            order.payments.addAll(payments);
        }
        order.cancelReason = cancelReason;
        
        return order;
    }

    /**
     * 결제 추가 및 주문 확정
     * @param paymentMethodType 결제 방법
     * @param paymentAmount 결제 금액
     * @param idGenerator ID 생성기
     */
    public void confirmWithPayment(
            PaymentMethodType paymentMethodType,
            Money paymentAmount,
            IdGenerator idGenerator) {
        
        validateCanBeConfirmed();
        
        // TODO: 결제 처리 로직 구현
        OrderPayment payment = OrderPayment.create(
                this.id.getValue(),
                paymentMethodType,
                paymentAmount,
                idGenerator
        );
        
        this.payments.add(payment);
        this.status = OrderStatus.PAID;
        this.modifiedAt = LocalDateTime.now();
        
        // 모든 주문 항목도 확정 상태로 변경
        confirmAllOrderItems();
        
        raiseOrderPaidEvent(paymentMethodType);
    }

    /**
     * 주문 취소
     * @param reason 취소 사유
     */
    public void cancel(String reason) {
        validateCanBeCancelled();

        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
        this.modifiedAt = LocalDateTime.now();

        cancelAllOrderItems();
        raiseOrderCancelledEvent(reason);
    }

    /**
     * 배송 시작
     * @param trackingNumber 운송장 번호
     */
    public void startShipping(String trackingNumber) {
        validateCanBeShipped();
        
        this.status = OrderStatus.SHIPPING;
        this.modifiedAt = LocalDateTime.now();
        
        startShippingForAllItems();
    }

    /**
     * 배송 완료
     */
    public void completeDelivery() {
        validateCanBeDelivered();
        
        this.status = OrderStatus.DELIVERED;
        this.modifiedAt = LocalDateTime.now();
        
        completeDeliveryForAllItems();
    }

    /**
     * 주문 완료 (구매 확정)
     */
    public void complete() {
        validateCanBeCompleted();
        
        this.status = OrderStatus.COMPLETED;
        this.modifiedAt = LocalDateTime.now();
    }

    // Private Helper Methods
    private void validateCanBeConfirmed() {
        if (!canBeConfirmed()) {
            throw new IllegalStateException(
                String.format(CANNOT_BE_CONFIRMED_MESSAGE, this.status)
            );
        }
    }
    
    private void validateCanBeCancelled() {
        if (!canBeCancelled()) {
            throw new IllegalStateException(
                String.format(CANNOT_BE_CANCELLED_MESSAGE, this.status)
            );
        }
    }
    
    private void validateCanBeShipped() {
        if (!canBeShipped()) {
            throw new IllegalStateException(
                String.format(CANNOT_BE_SHIPPED_MESSAGE, this.status)
            );
        }
    }
    
    private void validateCanBeDelivered() {
        if (!canBeDelivered()) {
            throw new IllegalStateException(
                String.format(CANNOT_BE_DELIVERED_MESSAGE, this.status)
            );
        }
    }
    
    private void validateCanBeCompleted() {
        if (!canBeCompleted()) {
            throw new IllegalStateException(
                String.format(CANNOT_BE_COMPLETED_MESSAGE, this.status)
            );
        }
    }

    private boolean canBeConfirmed() {
        return this.status == OrderStatus.PENDING;
    }

    private boolean canBeCancelled() {
        return this.status.isCancellable();
    }

    private boolean canBeShipped() {
        return this.status == OrderStatus.PAID;
    }
    
    private boolean canBeDelivered() {
        return this.status == OrderStatus.SHIPPING;
    }
    
    private boolean canBeCompleted() {
        return this.status == OrderStatus.DELIVERED;
    }
    
    private void cancelAllOrderItems() {
        orderItems.forEach(OrderItem::cancel);
    }
    
    private void confirmAllOrderItems() {
        orderItems.forEach(OrderItem::confirmPayment);
    }
    
    private void startShippingForAllItems() {
        orderItems.forEach(OrderItem::startShipping);
    }
    
    private void completeDeliveryForAllItems() {
        orderItems.forEach(OrderItem::completeDelivery);
    }

    private static void validateOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
    }

    private static Money calculateTotalAmount(List<OrderItem> orderItems) {
        BigDecimal total = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .map(Money::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Money.of(total);
    }

    // Domain Events
    
    private void raiseOrderCreatedEvent() {
        addDomainEvent(new OrderCreatedEvent(
                this.id,
                this.memberId, 
                this.orderNumber,
                this.totalAmount,
                this.orderedAt
        ));
    }
    
    private void raiseOrderPaidEvent(PaymentMethodType paymentMethodType) {
        addDomainEvent(new OrderPaidEvent(
                this.id,
                this.memberId, 
                this.orderNumber,
                this.totalAmount,
                paymentMethodType,
                LocalDateTime.now()
        ));
    }
    
    private void raiseOrderCancelledEvent(String reason) {
        addDomainEvent(new OrderCancelledEvent(
                this.id,
                this.memberId, 
                this.orderNumber,
                this.totalAmount,
                reason,
                LocalDateTime.now()
        ));
    }
    
    private void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    // Getters with defensive copying
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }
    
    public List<OrderPayment> getPayments() {
        return Collections.unmodifiableList(payments);
    }
    
    /**
     * 완료된 결제 정보 조회
     */
    public OrderPayment getCompletedPayment() {
        return payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
                .findFirst()
                .orElse(null);
    }

    /**
     * 할인 전 원래 금액 계산 (모든 상품의 정가 합계)
     * DB의 total_amount 컬럼에 매핑됨
     */
    public Money getOriginalAmount() {
        BigDecimal originalTotal = orderItems.stream()
                .map(item -> item.getProductPrice().getAmount()
                        .multiply(BigDecimal.valueOf(item.getQuantity().value())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Money.of(originalTotal);
    }

    /**
     * 총 할인 금액 계산 (모든 항목의 할인액 합계)
     * DB의 total_discount 컬럼에 매핑됨
     */
    public Money getDiscountAmount() {
        BigDecimal totalDiscount = orderItems.stream()
                .map(item -> item.getProductDiscountPrice().getAmount()
                        .multiply(BigDecimal.valueOf(item.getQuantity().value())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Money.of(totalDiscount);
    }

    /**
     * 최종 결제 금액 (할인 적용 후)
     * DB의 pay_amount 컬럼에 매핑됨
     */
    public Money getPayAmount() {
        return this.totalAmount; // 이미 할인이 적용된 최종 금액
    }

    /**
     * 금액 계산 검증
     * 원래금액 - 할인금액 = 결제금액 인지 확인
     */
    public boolean validateAmounts() {
        Money original = getOriginalAmount();
        Money discount = getDiscountAmount();
        Money payment = getPayAmount();
        
        Money calculated = Money.of(original.getAmount().subtract(discount.getAmount()));
        return calculated.equals(payment);
    }
}
