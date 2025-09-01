package innercircle.commerce.order.domain.model.vo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OrderStatus Enum
 * 주문 전체 상태
 */
@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    PENDING("대기중", "주문이 생성되었으나 아직 확정되지 않음"),
    PAID("결제완료", "주문이 확정되고 결제가 완료됨"),
    PREPARING("준비중", "상품 준비중"),
    SHIPPING("배송중", "배송이 시작됨"),
    DELIVERED("배송완료", "배송이 완료됨"),
    COMPLETED("완료", "구매가 확정됨"),
    CANCELLED("취소", "주문이 취소됨"),
    REFUNDED("환불", "환불이 완료됨");

    private final String description;
    private final String detail;

    /**
     * 다음 상태로 전환 가능한지 확인
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == PAID || newStatus == CANCELLED;
            case PAID:
                return newStatus == PREPARING || newStatus == CANCELLED;
            case PREPARING:
                return newStatus == SHIPPING || newStatus == CANCELLED;
            case SHIPPING:
                return newStatus == DELIVERED;
            case DELIVERED:
                return newStatus == COMPLETED || newStatus == REFUNDED;
            case COMPLETED:
                return newStatus == REFUNDED;
            case CANCELLED:
            case REFUNDED:
                return false; // 최종 상태
            default:
                return false;
        }
    }

    /**
     * 취소 가능한 상태인지 확인
     */
    public boolean isCancellable() {
        return this == PENDING || this == PAID || this == PREPARING;
    }

    /**
     * 환불 가능한 상태인지 확인
     */
    public boolean isRefundable() {
        return this == DELIVERED || this == COMPLETED;
    }
}
