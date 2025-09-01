package innercircle.commerce.order.domain.event;

import innercircle.commerce.order.domain.model.vo.MemberId;
import innercircle.commerce.order.domain.model.vo.Money;
import innercircle.commerce.order.domain.model.vo.OrderId;
import innercircle.commerce.order.domain.model.vo.OrderNumber;
import innercircle.commerce.order.domain.model.vo.enums.PaymentMethodType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * OrderPaidEvent
 * 주문 결제 완료 이벤트
 */
@Getter
public class OrderPaidEvent extends DomainEvent {
    
    private final OrderId orderId;
    private final MemberId memberId;
    private final OrderNumber orderNumber;
    private final Money totalAmount;
    private final PaymentMethodType paymentMethodType;
    private final LocalDateTime paidAt;
    
    public OrderPaidEvent(
            OrderId orderId,
            MemberId memberId,
            OrderNumber orderNumber,
            Money totalAmount,
            PaymentMethodType paymentMethodType,
            LocalDateTime paidAt) {
        super();
        this.orderId = orderId;
        this.memberId = memberId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.paymentMethodType = paymentMethodType;
        this.paidAt = paidAt;
    }
    
    @Override
    public String getAggregateId() {
        return orderId.toString();
    }
    
    @Override
    public String getEventType() {
        return "OrderPaid";
    }
}
