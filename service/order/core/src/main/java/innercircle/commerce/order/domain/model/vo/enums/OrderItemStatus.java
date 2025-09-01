package innercircle.commerce.order.domain.model.vo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OrderItemStatus Enum
 * 주문 상품 상태
 */
@Getter
@RequiredArgsConstructor
public enum OrderItemStatus {
    PENDING("대기중", "주문 항목이 생성됨"),
    CONFIRMED("확정", "결제가 완료됨"),
    PREPARING("준비중", "상품 준비중"),
    SHIPPING("배송중", "배송이 시작됨"),
    DELIVERED("배송완료", "배송이 완료됨"),
    CANCELLED("취소", "주문 항목이 취소됨"),
    REFUNDED("환불", "환불이 완료됨"),
    EXCHANGED("교환", "교환이 완료됨"),
    RETURNED("반품", "반품이 완료됨");

    private final String description;
    private final String detail;

    /**
     * 취소 가능한 상태인지 확인
     */
    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED || this == PREPARING;
    }

    /**
     * 환불 가능한 상태인지 확인
     */
    public boolean isRefundable() {
        return this == DELIVERED;
    }

    /**
     * 교환 가능한 상태인지 확인
     */
    public boolean isExchangeable() {
        return this == DELIVERED;
    }

    /**
     * 취소된 상태인지 확인
     */
    public boolean isCancelled() {
        return this == CANCELLED;
    }

}
