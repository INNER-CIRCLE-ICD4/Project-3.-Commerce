package innercircle.commerce.order.domain.model.vo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ShippingStatus Enum
 * 배송 상태
 */
@Getter
@RequiredArgsConstructor
public enum ShippingStatus {
    READY("준비", "배송 준비 중"),
    SHIPPED("출고", "배송이 시작됨"),
    IN_TRANSIT("운송중", "배송 중"),
    OUT_FOR_DELIVERY("배송중", "배송기사가 배송 중"),
    DELIVERED("완료", "배송이 완료됨"),
    FAILED("실패", "배송 실패"),
    RETURNED("반송", "배송 반송됨");

    private final String description;
    private final String detail;

    /**
     * 다음 상태로 전환 가능한지 확인
     */
    public boolean canTransitionTo(ShippingStatus newStatus) {
        switch (this) {
            case READY:
                return newStatus == SHIPPED;
            case SHIPPED:
                return newStatus == IN_TRANSIT || newStatus == FAILED || newStatus == RETURNED;
            case IN_TRANSIT:
                return newStatus == OUT_FOR_DELIVERY || newStatus == FAILED || newStatus == RETURNED;
            case OUT_FOR_DELIVERY:
                return newStatus == DELIVERED || newStatus == FAILED || newStatus == RETURNED;
            case DELIVERED:
            case FAILED:
            case RETURNED:
                return false; // 최종 상태
            default:
                return false;
        }
    }
    
    /**
     * 최종 상태인지 확인
     */
    public boolean isFinal() {
        return this == DELIVERED || this == FAILED || this == RETURNED;
    }
    
    /**
     * 성공적인 배송인지 확인
     */
    public boolean isSuccessful() {
        return this == DELIVERED;
    }
}
