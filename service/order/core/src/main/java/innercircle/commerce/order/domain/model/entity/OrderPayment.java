package innercircle.commerce.order.domain.model.entity;

import innercircle.commerce.order.domain.model.vo.*;
import innercircle.commerce.order.domain.model.vo.enums.PaymentMethodType;
import innercircle.commerce.order.domain.model.vo.enums.PaymentStatus;
import innercircle.commerce.order.domain.services.IdGenerator;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * OrderPayment Entity
 * 주문 결제 정보
 */
@Getter
public class OrderPayment {
    
    private final OrderPaymentId id;
    private final Long orderId;
    private final PaymentMethodType paymentMethod;
    private final Money paymentAmount;
    private final PaymentStatus status;
    private final String transactionId;
    private final LocalDateTime paidAt;

    private OrderPayment(
            OrderPaymentId id,
            Long orderId,
            PaymentMethodType paymentMethod,
            Money paymentAmount,
            PaymentStatus status,
            String transactionId,
            LocalDateTime paidAt) {
        this.id = id;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
        this.status = status;
        this.transactionId = transactionId;
        this.paidAt = paidAt;
    }

    /**
     * 새 결제 생성
     */
    public static OrderPayment create(
            Long orderId,
            PaymentMethodType paymentMethod,
            Money amount,
            IdGenerator idGenerator) {
        
        // TODO: 실제 결제 API 연동 구현
        return new OrderPayment(
                OrderPaymentId.of(idGenerator.generateId()),
                orderId,
                paymentMethod,
                amount,
                PaymentStatus.COMPLETED, // TODO: 실제 결제 상태로 변경
                "TXN_" + System.currentTimeMillis(), // TODO: 실제 거래ID로 변경
                LocalDateTime.now()
        );
    }
    
    /**
     * 기존 결제 복원
     */
//    public static OrderPayment restore(
//            OrderPaymentId id,
//            Long orderId,
//            PaymentMethodType paymentMethod,
//            Money paymentAmount,
//            PaymentStatus status,
//            String transactionId,
//            LocalDateTime paidAt) {
//
//        return new OrderPayment(
//                id,
//                orderId,
//                paymentMethod,
//                paymentAmount,
//                status,
//                transactionId,
//                paidAt
//        );
//    }
}
