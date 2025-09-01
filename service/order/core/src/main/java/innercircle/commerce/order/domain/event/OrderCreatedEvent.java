package innercircle.commerce.order.domain.event;

import innercircle.commerce.order.domain.model.vo.MemberId;
import innercircle.commerce.order.domain.model.vo.Money;
import innercircle.commerce.order.domain.model.vo.OrderId;
import innercircle.commerce.order.domain.model.vo.OrderNumber;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * OrderCreatedEvent
 * 주문 생성 이벤트
 */
@Getter
public class OrderCreatedEvent extends DomainEvent {
    
    private final OrderId orderId;
    private final MemberId memberId;
    private final OrderNumber orderNumber;
    private final Money totalAmount;
    private final LocalDateTime orderedAt;
    
    public OrderCreatedEvent(
            OrderId orderId,
            MemberId memberId,
            OrderNumber orderNumber,
            Money totalAmount,
            LocalDateTime orderedAt) {
        super();
        this.orderId = orderId;
        this.memberId = memberId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.orderedAt = orderedAt;
    }
    
    @Override
    public String getAggregateId() {
        return orderId.toString();
    }
    
    @Override
    public String getEventType() {
        return "OrderCreated";
    }
}
