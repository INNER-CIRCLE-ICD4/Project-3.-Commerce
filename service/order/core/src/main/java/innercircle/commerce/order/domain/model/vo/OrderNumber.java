package innercircle.commerce.order.domain.model.vo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * OrderNumber Value Object
 * 주문번호
 */
public class OrderNumber {
    private final String value;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public OrderNumber(String value) {
        validate(value);
        this.value = value;
    }

    public static OrderNumber of(String value) {
        return new OrderNumber(value);
    }

    /**
     * 주문번호 생성
     * 형식: ORD-YYYYMMDDHHMMSS-XXXX (4자리 랜덤)
     */
    public static OrderNumber generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return new OrderNumber(String.format("ORD-%s-%04d", timestamp, random));
    }

    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderNumber cannot be null or empty");
        }
        
        if (!value.matches("^ORD-\\d{14}-\\d{4}$")) {
            throw new IllegalArgumentException("Invalid OrderNumber format");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderNumber that = (OrderNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
