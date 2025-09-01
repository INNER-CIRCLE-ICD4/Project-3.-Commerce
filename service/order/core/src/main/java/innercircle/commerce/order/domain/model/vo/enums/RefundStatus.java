package innercircle.commerce.order.domain.model.vo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RefundStatus Enum
 * 환불 상태
 */
@Getter
@RequiredArgsConstructor
public enum RefundStatus {
    REQUESTED("요청됨", "환불이 요청됨"),
    APPROVED("승인됨", "환불이 승인됨"),
    REJECTED("거절됨", "환불이 거절됨"),
    COMPLETED("완료됨", "환불이 완료됨"),
    CANCELLED("취소됨", "환불 요청이 취소됨");

    private final String description;
    private final String detail;

    /**
     * 환불 처리 가능한 상태인지 확인
     */
    public boolean canProcess() {
        return this == REQUESTED;
    }
    
    /**
     * 완료 가능한 상태인지 확인
     */
    public boolean canComplete() {
        return this == APPROVED;
    }
    
    /**
     * 최종 상태인지 확인
     */
    public boolean isFinal() {
        return this == COMPLETED || this == REJECTED || this == CANCELLED;
    }
}
