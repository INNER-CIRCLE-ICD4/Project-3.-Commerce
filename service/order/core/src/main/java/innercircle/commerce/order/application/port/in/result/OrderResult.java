package innercircle.commerce.order.application.port.in.result;

import innercircle.commerce.order.domain.model.aggregate.Order;
import innercircle.commerce.order.domain.model.entity.OrderItem;
import innercircle.commerce.order.domain.model.vo.ShippingAddress;
import innercircle.commerce.order.domain.model.vo.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderResult
 * 주문 결과를 반환하는 DTO
 */
public record OrderResult(
        Long orderId,
        String orderNumber,
        Long memberId,
        BigDecimal totalAmount,
        OrderStatus status,
        ShippingAddressResult shippingAddress,
        List<OrderItemResult> orderItems,
        LocalDateTime orderedAt
) {
    public static OrderResult from(Order order) {
        return new OrderResult(
                order.getId().getValue(),
                order.getOrderNumber().getValue(),
                order.getMemberId().getValue(),
                order.getPayAmount().getAmount(),
                order.getStatus(),
                ShippingAddressResult.from(order.getShippingAddress()),
                order.getOrderItems().stream().map(OrderItemResult::from).toList(),
                order.getOrderedAt()
        );
    }

    public record ShippingAddressResult(
            String recipientName,
            String phoneNumber,
            String fullAddress
    ) {
        public static ShippingAddressResult from(ShippingAddress address) {
            return new ShippingAddressResult(
                    address.recipientName(),
                    address.phoneNumber(),
                    address.fullAddress()
            );
        }
    }

    public record OrderItemResult(
            Long orderItemId,
            Long productId,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal,
            String status
    ) {
        public static OrderItemResult from(OrderItem item) {
            return new OrderItemResult(
                    item.getId().getValue(),
                    item.getProductId().getValue(),
                    item.getProductName(),
                    item.getProductPrice().getAmount(),
                    item.getQuantity().value(),
                    item.getTotalPrice().getAmount(),
                    item.getStatus().name()
            );
        }
    }
}
