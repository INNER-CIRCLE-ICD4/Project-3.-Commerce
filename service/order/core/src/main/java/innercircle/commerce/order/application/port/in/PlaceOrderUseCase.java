package innercircle.commerce.order.application.port.in;

import innercircle.commerce.order.application.port.in.command.PlaceOrderCommand;
import innercircle.commerce.order.application.port.in.result.OrderResult;

/**
 * PlaceOrderUseCase Interface
 * 주문 생성 유스케이스의 입력 포트
 */
public interface PlaceOrderUseCase {
    /**
     * 새로운 주문을 생성
     *
     * @param command 주문 생성 명령
     * @return 생성된 주문 결과
     */
    OrderResult placeOrder(PlaceOrderCommand command);
}
