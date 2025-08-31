package innercircle.commerce.order.domain.event;

import innercircle.commerce.order.domain.model.vo.MemberId;
import innercircle.commerce.order.domain.model.vo.Money;
import innercircle.commerce.order.domain.model.vo.OrderId;
import innercircle.commerce.order.domain.model.vo.OrderNumber;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * OrderCancelledEvent
 * 주문 취소 이벤트
 */
@Getter
public class OrderCancelledEvent extends DomainEvent {
    
    private final OrderId orderId;
    private final MemberId memberId;
    private final OrderNumber orderNumber;
    private final Money totalAmount;
    private final String reason;
    private final LocalDateTime cancelledAt;
    
    public OrderCancelledEvent(
            OrderId orderId,
            MemberId memberId,
            OrderNumber orderNumber,
            Money totalAmount,
            String reason,
            LocalDateTime cancelledAt) {
        super();
        this.orderId = orderId;
        this.memberId = memberId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.reason = reason;
        this.cancelledAt = cancelledAt;
    }
    
    @Override
    public String getAggregateId() {
        return orderId.toString();
    }
    
    @Override
    public String getEventType() {
        return "OrderCancelled";
    }
}
