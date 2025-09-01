package innercircle.commerce.order.domain.model.vo;

import innercircle.commerce.order.domain.model.vo.enums.PaymentMethodType;
import innercircle.commerce.order.domain.model.vo.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * PaymentInfo Value Object
 * 결제 정보
 */
@Getter
@Builder
public class PaymentInfo {
    private final PaymentMethodType paymentMethodType;
    private final String paymentMethodDetail;
    private final Money amount;
    private final LocalDateTime paidAt;
    private final String transactionId;
    private final PaymentStatus status;

    public PaymentInfo(
            PaymentMethodType paymentMethodType,
            String paymentMethodDetail,
            Money amount,
            LocalDateTime paidAt,
            String transactionId,
            PaymentStatus status) {
        this.paymentMethodType = paymentMethodType;
        this.paymentMethodDetail = paymentMethodDetail;
        this.amount = amount;
        this.paidAt = paidAt;
        this.transactionId = transactionId;
        this.status = status;
        validate();
    }

    private void validate() {
        if (paymentMethodType == null) {
            throw new IllegalArgumentException("Payment method type is required");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Payment amount is required");
        }
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Payment status is required");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentInfo that = (PaymentInfo) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
}
