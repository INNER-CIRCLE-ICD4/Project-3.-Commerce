package innercircle.commerce.order.application.usecases;

import innercircle.commerce.order.application.port.in.PlaceOrderUseCase;
import innercircle.commerce.order.application.port.in.command.PlaceOrderCommand;
import innercircle.commerce.order.application.port.in.result.OrderResult;
import innercircle.commerce.order.application.port.out.EventPublisher;
import innercircle.commerce.order.application.port.out.OrderRepositoryPort;
import innercircle.commerce.order.application.port.out.ProductService;
import innercircle.commerce.order.domain.model.aggregate.Order;
import innercircle.commerce.order.domain.model.entity.OrderItem;
import innercircle.commerce.order.domain.model.vo.*;
import innercircle.commerce.order.domain.model.vo.enums.PaymentMethodType;
import innercircle.commerce.order.domain.services.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PlaceOrderService
 * 주문 생성 유스케이스 구현체
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final ProductService productService;
    private final EventPublisher eventPublisher;
    private final IdGenerator idGenerator;

    @Override
    public OrderResult placeOrder(PlaceOrderCommand command) {
        log.info("Placing order (instant payment). memberId={}", command.memberId());

        // 1) 재고 확인 + 예약
        command.orderItems().forEach(this::validateOrderAvailability);
        command.orderItems().forEach(this::reserveStock);

        // 2) 주문 생성
        ShippingAddress addr = toShippingAddress(command.shippingInfo());
        List<OrderItem> items = command.orderItems().stream().map(this::toOrderItem).toList();
        Order order = Order.createNew(
                MemberId.of(command.memberId()), addr, items, idGenerator);

        // 3) 결제 즉시 확정 (Aggregate 내부에 Payment 포함, 별도 repo X)
        PaymentMethodType method = PaymentMethodType.valueOf(command.paymentMethod().name());
        order.confirmWithPayment(method, order.getTotalAmount(), idGenerator);

        // 4) 저장 (order + items + payments cascade)
        Order saved = orderRepository.save(order);

        // 5) 이벤트 발행 (실패해도 흐름 유지)
        publishOrderEvents(saved);

        log.info("Order placed & paid. orderNumber={}", saved.getOrderNumber().getValue());
        return OrderResult.from(saved);
    }

    /* ---------- mapping / helpers ---------- */

    private ShippingAddress toShippingAddress(PlaceOrderCommand.ShippingInfo shippingInfo) {
        return ShippingAddress.create(shippingInfo);
    }

    private OrderItem toOrderItem(PlaceOrderCommand.OrderItemCommand c) {
        // ProductService를 통해 상품 정보 조회
        ProductService.ProductInfo productInfo = productService.getProductInfo(ProductId.of(c.productId()));
        ProductService.ProductOptionInfo optionInfo = productService.getProductOptionInfo(
            ProductId.of(c.productId()), c.productOptionId());
        
        if (optionInfo.getDiscountPrice().getValue().compareTo(optionInfo.getPrice().getValue()) > 0) {
            throw new IllegalArgumentException("discount per unit cannot exceed unit price");
        }
        
        return OrderItem.create(
                OrderItemId.of(idGenerator.generateId()),
                ProductId.of(c.productId()),
                productInfo.getName(),
                Money.of(optionInfo.getPrice().getValue()),
                Quantity.of(c.quantity()),
                ProductOption.of(c.productOptionId(), optionInfo.getName()),
                Money.of(optionInfo.getDiscountPrice().getValue())
        );
    }

    private void validateOrderAvailability(PlaceOrderCommand.OrderItemCommand c) {
        boolean ok = productService.checkAvailability(ProductId.of(c.productId()), c.quantity());
        if (!ok) {
            throw new IllegalStateException("Out of stock: productId=" + c.productId());
        }
    }

    private void reserveStock(PlaceOrderCommand.OrderItemCommand c) {
        try {
            productService.reserveStock(ProductId.of(c.productId()), c.quantity());
        } catch (Exception e) {
            throw new IllegalStateException("Reserve failed: productId=" + c.productId(), e);
        }
    }

    private void publishOrderEvents(Order order) {
        order.getDomainEvents().forEach(evt -> {
            try {
                eventPublisher.publish(evt);
            } catch (Exception e) {
                log.warn("Event publish failed (ignored). type={}", evt.getEventType(), e);
            }
        });
        order.clearDomainEvents();
    }

}
