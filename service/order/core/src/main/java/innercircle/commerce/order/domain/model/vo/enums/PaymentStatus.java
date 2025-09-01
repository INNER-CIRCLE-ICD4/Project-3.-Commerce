package innercircle.commerce.order.domain.model.vo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PaymentStatus
 * 결제 상태
 */
@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("대기중"),
    PROCESSING("처리중"),
    COMPLETED("완료"),
    FAILED("실패"),
    CANCELLED("취소"),
    REFUNDED("환불"),
    PARTIAL_REFUNDED("부분환불");

    private final String description;

    /**
     * 결제 완료 상태인지 확인
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    /**
     * 결제 실패 상태인지 확인
     */
    public boolean isFailed() {
        return this == FAILED || this == CANCELLED;
    }
    
    /**
     * 환불 가능한 상태인지 확인
     */
    public boolean isRefundable() {
        return this == COMPLETED;
    }
}
