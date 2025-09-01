package innercircle.commerce.order.domain.model.entity;

import innercircle.commerce.order.domain.model.vo.Money;
import innercircle.commerce.order.domain.model.vo.enums.RefundStatus;
import innercircle.commerce.order.domain.services.IdGenerator;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * OrderRefund Entity
 * 주문 환불/반품 정보 관리 엔티티
 */
@Getter
public class OrderRefund {
    
    private final Long id;
    private final Long orderItemId;
    private final String reason;
    private final Money refundAmount;
    private RefundStatus status;
    private final LocalDateTime requestedAt;
    private LocalDateTime refundedAt;
    
    // Private Constructor
    private OrderRefund(
            Long id,
            Long orderItemId,
            String reason,
            Money refundAmount,
            RefundStatus status,
            LocalDateTime requestedAt,
            LocalDateTime refundedAt) {
        this.id = id;
        this.orderItemId = orderItemId;
        this.reason = reason;
        this.refundAmount = refundAmount;
        this.status = status;
        this.requestedAt = requestedAt;
        this.refundedAt = refundedAt;
    }
    
    /**
     * 새로운 환불 요청 생성
     */
    public static OrderRefund create(
            Long orderItemId,
            String reason,
            Money refundAmount,
            IdGenerator idGenerator) {
        
        return new OrderRefund(
                idGenerator.generateId(),
                orderItemId,
                reason,
                refundAmount,
                RefundStatus.REQUESTED,
                LocalDateTime.now(),
                null
        );
    }
    
    /**
     * 기존 환불 복원
     */
    public static OrderRefund restore(
            Long id,
            Long orderItemId,
            String reason,
            Money refundAmount,
            RefundStatus status,
            LocalDateTime requestedAt,
            LocalDateTime refundedAt) {
        
        return new OrderRefund(
                id,
                orderItemId,
                reason,
                refundAmount,
                status,
                requestedAt,
                refundedAt
        );
    }
    
    /**
     * 환불 승인
     */
    public void approve() {
        // TODO: 환불 승인 로직 구현
        validateCanApprove();
        
        this.status = RefundStatus.APPROVED;
    }
    
    /**
     * 환불 완료
     */
    public void complete() {
        // TODO: 환불 완료 로직 구현
        validateCanComplete();
        
        this.status = RefundStatus.COMPLETED;
        this.refundedAt = LocalDateTime.now();
    }
    
    /**
     * 환불 거절
     */
    public void reject() {
        // TODO: 환불 거절 로직 구현
        validateCanReject();
        
        this.status = RefundStatus.REJECTED;
    }
    
    private void validateCanApprove() {
        if (this.status != RefundStatus.REQUESTED) {
            throw new IllegalStateException("Refund can only be approved from REQUESTED status");
        }
    }
    
    private void validateCanComplete() {
        if (this.status != RefundStatus.APPROVED) {
            throw new IllegalStateException("Refund can only be completed from APPROVED status");
        }
    }
    
    private void validateCanReject() {
        if (this.status != RefundStatus.REQUESTED) {
            throw new IllegalStateException("Refund can only be rejected from REQUESTED status");
        }
    }
}
