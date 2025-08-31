package innercircle.commerce.order.application.usecases;

import innercircle.commerce.order.application.port.in.CancelOrderUseCase;
import innercircle.commerce.order.application.port.out.OrderRepositoryPort;
import innercircle.commerce.order.domain.model.aggregate.Order;
import innercircle.commerce.order.domain.model.vo.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    /**
     * 전체 주문 취소
     */
    @Override
    public void cancelEntireOrder(Long orderId, String reason) {
        log.info("Cancelling entire order: {} with reason: {}", orderId, reason);

        Order order = orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.cancel(reason);
        orderRepository.save(order);

        log.info("Order cancelled successfully: {}", orderId);
    }


    /**
     * 개별 상품 취소
     */
    @Override
    public void cancelOrderItem(Long orderId, Long orderItemId, String reason) {
        log.info("Cancelling order item: {} from order: {} with reason: {}",
                orderItemId, orderId, reason);

        Order order = orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.cancel(reason);
        orderRepository.save(order);

        log.info("Order item cancelled successfully: {}", orderItemId);
    }
}
