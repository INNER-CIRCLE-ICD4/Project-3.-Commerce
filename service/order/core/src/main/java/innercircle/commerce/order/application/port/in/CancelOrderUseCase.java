package innercircle.commerce.order.application.port.in;


/**
 * CancelOrderUseCase Interface
 * 반품/환불 유스케이스의 입력 포트
 */
public interface CancelOrderUseCase {

    /**
     * 전체 주문 취소
     */
    void cancelEntireOrder(Long orderId, String reason);

    /**
     * 개별 상품 취소
     */
    void cancelOrderItem(Long orderId, Long orderItemId, String reason);

}
