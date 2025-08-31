package innercircle.commerce.order.application.usecases;

import innercircle.commerce.order.application.port.in.GetOrderUseCase;
import innercircle.commerce.order.application.port.out.OrderRepositoryPort;
import innercircle.commerce.order.domain.model.aggregate.Order;
import innercircle.commerce.order.domain.model.vo.MemberId;
import innercircle.commerce.order.domain.model.vo.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    /**
     * 주문 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    /**
     * 회원별 주문 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getMemberOrders(Long memberId) {
        return orderRepository.findByMemberId(MemberId.of(memberId));
    }
}
