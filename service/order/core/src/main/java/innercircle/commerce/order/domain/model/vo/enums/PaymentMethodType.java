package innercircle.commerce.order.domain.model.vo.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PaymentMethodType
 * 결제 방법
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethodType {
    CREDIT_CARD("신용카드"),
    DEBIT_CARD("체크카드"),
    BANK_TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE_PAYMENT("모바일결제"),
    KAKAO_PAY("카카오페이"),
    NAVER_PAY("네이버페이"),
    PAYCO("페이코"),
    SAMSUNG_PAY("삼성페이"),
    POINT("포인트"),
    COUPON("쿠폰"),
    CASH("현금");

    private final String description;

    /**
     * 온라인 결제 방법인지 확인
     */
    public boolean isOnlinePayment() {
        return this != CASH;
    }

    /**
     * 모바일 결제 방법인지 확인
     */
    public boolean isMobilePayment() {
        return this == MOBILE_PAYMENT || this == KAKAO_PAY ||
                this == NAVER_PAY || this == PAYCO || this == SAMSUNG_PAY;
    }
}
