package innercircle.commerce.order.infra.adapter.persistence.mapper;

import innercircle.commerce.order.domain.model.aggregate.Order;
import innercircle.commerce.order.domain.model.entity.OrderItem;
import innercircle.commerce.order.domain.model.entity.OrderPayment;
import innercircle.commerce.order.domain.model.vo.*;
import innercircle.commerce.order.domain.model.vo.enums.OrderItemStatus;
import innercircle.commerce.order.domain.model.vo.enums.OrderStatus;
import innercircle.commerce.order.infra.adapter.persistence.entity.OrderEntity;
import innercircle.commerce.order.infra.adapter.persistence.entity.OrderItemEntity;
import innercircle.commerce.order.infra.adapter.persistence.entity.PaymentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderMapper
 * 도메인 모델과 JPA 엔티티 간의 변환 담당
 */
@Slf4j
@Component
public class OrderMapper {

    /**
     * 도메인 Order를 JPA OrderEntity로 변환
     */
    public OrderEntity toEntity(Order order) {
        if (order == null) {
            return null;
        }

        // 배송 주소 정보 추출
        ShippingAddress shipping = order.getShippingAddress();

        OrderEntity entity = new OrderEntity(
                order.getId() != null ? order.getId().getValue() : null,
                order.getMemberId().getValue(),
                order.getOrderNumber().getValue(),
                order.getOrderedAt(),
                shipping.recipientName(),
                shipping.phoneNumber(),
                shipping.addressCode(),
                shipping.address(),
                shipping.addressDetail(),
                order.getOriginalAmount().getAmount(),  // DB의 total_amount: 할인 전 금액
                order.getDiscountAmount().getAmount(),  // DB의 total_discount: 총 할인액
                order.getTotalAmount().getAmount(),     // DB의 pay_amount: 최종 결제액
                order.getStatus().name()
        );

        // 주문 항목들 변환 및 추가
        order.getOrderItems().forEach(item -> {
            OrderItemEntity itemEntity = toItemEntity(item, entity);
            entity.addItem(itemEntity);
        });

        return entity;
    }

    /**
     * JPA OrderEntity를 도메인 Order로 변환
     */
    public Order toDomain(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        // Value Objects 생성
        OrderId orderId = OrderId.of(entity.getId());
        OrderNumber orderNumber = OrderNumber.of(entity.getOrderNumber());
        MemberId memberId = MemberId.of(entity.getMemberId());
        Money totalAmount = Money.of(entity.getFinalAmount());

        // 배송 주소 생성
        ShippingAddress shippingAddress = new ShippingAddress(
                entity.getRecipientName(),
                entity.getRecipientPhone(),
                entity.getAddressCode(),
                entity.getAddress(),
                entity.getAddressDetail(),
                null // deliveryRequest는 DB에 저장되지 않음
        );

        // 주문 항목 변환
        List<OrderItem> orderItems = entity.getItems() != null ? 
                entity.getItems().stream()
                        .map(this::toItemDomain)
                        .collect(Collectors.toList()) :
                List.of();

        // 결제 정보 변환
        List<OrderPayment> payments = entity.getPayments() != null ?
                entity.getPayments().stream()
                        .map(this::toPaymentDomain)
                        .collect(Collectors.toList()) :
                List.of();

        // 주문 상태
        OrderStatus orderStatus = OrderStatus.valueOf(entity.getStatus());

        return Order.restore(
                orderId,
                orderNumber,
                memberId,
                shippingAddress,
                orderItems,
                totalAmount,
                orderStatus,
                entity.getOrderDate(),
                entity.getUpdatedAt(),
                payments,
                null // cancelReason
        );
    }

    /**
     * 도메인 OrderItem을 JPA OrderItemEntity로 변환
     */
    private OrderItemEntity toItemEntity(OrderItem item, OrderEntity orderEntity) {
        if (item == null) {
            return null;
        }

        // ProductOption 정보 추출
        ProductOption productOption = item.getProductOption();
        
        // Package private 생성자 사용
        return new OrderItemEntity(
                item.getId() != null ? item.getId().getValue() : null,
                orderEntity,
                item.getProductId().getValue(),
                item.getProductName(),
                item.getProductPrice().getAmount(),
                productOption != null ? productOption.getOptionId() : null,
                productOption != null ? productOption.getOptionName() : null,
                item.getProductDiscountPrice().getAmount(),
                item.getQuantity().value(),
                item.getTotalPrice().getAmount(),
                item.getStatus().name()
        );
    }

    /**
     * JPA OrderItemEntity를 도메인 OrderItem으로 변환
     */
    private OrderItem toItemDomain(OrderItemEntity entity) {
        if (entity == null) {
            return null;
        }

        // ProductOption 생성
        ProductOption productOption = ProductOption.of(
                entity.getProductOptionId(),
                entity.getProductOptionName()
        );

        // OrderItem 복원
        return OrderItem.restore(
                OrderItemId.of(entity.getId()),
                ProductId.of(entity.getProductId()),
                entity.getProductName(),
                Money.of(entity.getProductPrice()),
                Quantity.of(entity.getQuantity()),
                productOption,
                Money.of(entity.getProductDiscountPrice()),
                OrderItemStatus.valueOf(entity.getStatus())
        );
    }

    /**
     * JPA OrderPaymentEntity를 도메인 OrderPayment로 변환
     */
    private OrderPayment toPaymentDomain(PaymentEntity entity) {
        // TODO: OrderPayment 구현 완료 후 수정
        return null;
    }

    /**
     * 주문 상태 계산 로직
     */
    private OrderStatus calculateOrderStatus(List<OrderItem> orderItems, List<OrderPayment> payments) {
        if (orderItems.isEmpty()) {
            return OrderStatus.PENDING;
        }

        // 결제 완료 여부 확인
        boolean hasPaidPayment = payments.stream()
                .anyMatch(payment -> payment != null && payment.getStatus().isCompleted());

        if (!hasPaidPayment) {
            return OrderStatus.PENDING;
        }

        // 모든 상품이 취소된 경우
        boolean allCancelled = orderItems.stream()
                .allMatch(item -> item.getStatus().isCancelled());
        if (allCancelled) {
            return OrderStatus.CANCELLED;
        }

        // 일부 상품이 취소된 경우
        boolean someCancelled = orderItems.stream()
                .anyMatch(item -> item.getStatus().isCancelled());
        if (someCancelled) {
            return OrderStatus.CANCELLED;
        }

        return OrderStatus.PAID;
    }
}
