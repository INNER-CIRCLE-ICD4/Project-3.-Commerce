package innercircle.commerce.order.domain.model.vo;

import java.util.Objects;

/**
 * OrderId Value Object
 * 주문 고유 식별자
 */
public class OrderId {
    private final long value;

    private OrderId(long value) {
        this.value = value;
    }

    /**
     * OrderId 생성
     * @param value Snowflake ID
     * @return OrderId 인스턴스
     */
    public static OrderId of(long value) {
        return new OrderId(value);
    }
    
    public static OrderId of(String value) {
        return new OrderId(Long.parseLong(value));
    }

    public long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        OrderId orderId = (OrderId) o;
        return Objects.equals(value, orderId.value);
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
