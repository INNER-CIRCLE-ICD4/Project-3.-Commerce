package innercircle.commerce.order.domain.exception;

/**
 * OrderNotFoundException
 * 주문을 찾을 수 없을 때 발생하는 예외
 */
public class OrderNotFoundException extends DomainException {
    
    public OrderNotFoundException(String orderId) {
        super(String.format("Order not found with id: %s", orderId), "ORDER_NOT_FOUND");
    }
    
    public OrderNotFoundException(String field, String value) {
        super(String.format("Order not found with %s: %s", field, value), "ORDER_NOT_FOUND");
    }
}
