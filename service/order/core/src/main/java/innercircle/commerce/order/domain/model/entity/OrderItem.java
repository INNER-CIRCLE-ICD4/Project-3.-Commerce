package innercircle.commerce.order.domain.model.entity;

import innercircle.commerce.order.domain.event.DomainEvent;
import innercircle.commerce.order.domain.model.vo.*;
import innercircle.commerce.order.domain.model.vo.enums.OrderItemStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OrderItem
 * Order Aggregate 내부의 Entity
 * 주문 항목
 */
@Getter
public class OrderItem {

    private final OrderItemId id;
    private final ProductId productId;
    private final String productName;
    private final Money productPrice;
    private final Quantity quantity;
    private final ProductOption productOption;
    private final Money productDiscountPrice;
    private Money totalPrice;
    private OrderItemStatus status;

    // 이벤트/이력은 도메인 이벤트로 수집 (선택)
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private OrderItem(OrderItemId id,
                      ProductId productId,
                      String productName,
                      Money productPrice,
                      Quantity quantity,
                      ProductOption productOption,
                      Money productDiscountPrice,
                      OrderItemStatus status) {

        this.id = Objects.requireNonNull(id);
        this.productId = Objects.requireNonNull(productId);
        this.productName = Objects.requireNonNull(productName);
        this.productPrice = Objects.requireNonNull(productPrice);
        this.quantity = Objects.requireNonNull(quantity);
        this.productOption = Objects.requireNonNull(productOption);
        this.productDiscountPrice = productDiscountPrice != null ? productDiscountPrice : Money.zero();
        this.status = status != null ? status : OrderItemStatus.PENDING;
        this.totalPrice = calculateTotalPrice();
    }

    public static OrderItem create(OrderItemId id,
                                   ProductId productId,
                                   String productName,
                                   Money productPrice,
                                   Quantity quantity,
                                   ProductOption productOption,
                                   Money productDiscountPrice ) {

        if (productDiscountPrice.isGreaterThan(productPrice)) {
            throw new IllegalArgumentException("discount per unit cannot exceed unit price");
        }

        return new OrderItem(id, productId, productName, productPrice, quantity, productOption,
                productDiscountPrice, OrderItemStatus.PENDING);
    }

    public static OrderItem restore(OrderItemId id,
                                    ProductId productId,
                                    String productName,
                                    Money productPrice,
                                    Quantity quantity,
                                    ProductOption productOption,
                                    Money productDiscountPrice,
                                    OrderItemStatus status) {
        return new OrderItem(id, productId, productName, productPrice, quantity, productOption,
                productDiscountPrice, status);
    }

    public void confirmPayment() {
        if (status != OrderItemStatus.PENDING) {
            throw new IllegalStateException(String.format("Cannot confirm in %s", status));
        }
        changeStatus(OrderItemStatus.CONFIRMED, "결제 완료");
    }

    public void startShipping() {
        if (status != OrderItemStatus.CONFIRMED) {
            throw new IllegalStateException(String.format("Cannot ship in %s", status));
        }
        changeStatus(OrderItemStatus.SHIPPING, "배송 시작");
    }

    public void completeDelivery() {
        if (status != OrderItemStatus.SHIPPING) {
            throw new IllegalStateException(String.format("Cannot deliver in %s", status));
        }
        changeStatus(OrderItemStatus.DELIVERED, "배송 완료");
    }

    public void cancel() {
        if (!status.isCancellable()) {
            throw new IllegalStateException(String.format("Cannot cancel in %s", status));
        }
        changeStatus(OrderItemStatus.CANCELLED, "주문 취소");
    }

    private void changeStatus(OrderItemStatus newStatus, String note) {
        this.status = newStatus;
    }

    private Money calculateTotalPrice() {
        BigDecimal gross = productPrice.getAmount()
                .multiply(BigDecimal.valueOf(quantity.value()));
        BigDecimal discountPerUnit = productDiscountPrice.getAmount();
        BigDecimal discountTotal = discountPerUnit.multiply(BigDecimal.valueOf(quantity.value()));

        BigDecimal discounted = gross.subtract(discountTotal);
        if (discounted.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("totalPrice cannot be negative");
        }
        return Money.of(discounted);
    }

    public List<DomainEvent> pullDomainEvents() {
        var copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }
}

