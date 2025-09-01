package innercircle.commerce.order.domain.model.vo;

import java.util.Objects;

/**
 * OrderPaymentId Value Object
 * 주문 결제의 고유 식별자
 */
public class OrderPaymentId {
    private final long value;

    private OrderPaymentId(long value) {
        this.value = value;
    }

    /**
     * OrderPaymentId 생성
     * @param value Snowflake ID
     * @return OrderPaymentId 인스턴스
     */
    public static OrderPaymentId of(long value) {
        return new OrderPaymentId(value);
    }
    
    public static OrderPaymentId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("OrderPaymentId value cannot be null");
        }
        return new OrderPaymentId(value);
    }

    public long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        OrderPaymentId that = (OrderPaymentId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
