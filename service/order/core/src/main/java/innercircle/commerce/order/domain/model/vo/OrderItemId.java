package innercircle.commerce.order.domain.model.vo;

import java.util.Objects;

/**
 * OrderItemId Value Object
 * 주문 상품 고유 식별자
 */
public class OrderItemId {
    private final long value;

    private OrderItemId(long value) {
        this.value = value;
    }

    /**
     * OrderItemId 생성
     * @param value Snowflake ID
     * @return OrderItemId 인스턴스
     */
    public static OrderItemId of(long value) {
        return new OrderItemId(value);
    }
    
    public static OrderItemId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("OrderItemId value cannot be null");
        }
        return new OrderItemId(value);
    }

    public long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        OrderItemId that = (OrderItemId) o;
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
