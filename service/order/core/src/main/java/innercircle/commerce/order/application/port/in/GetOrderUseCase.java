package innercircle.commerce.order.application.port.in;

import innercircle.commerce.order.domain.model.aggregate.Order;

import java.util.List;

/**
 * CancelOrderUseCase Interface
 * 주문 조회 유스케이스의 입력 포트
 */
public interface GetOrderUseCase {

    /**
     * 주문 조회
     */
    Order getOrder(Long orderId);

    /**
     * 회원별 주문 목록 조회
     */
    List<Order> getMemberOrders(Long memberId);
}
