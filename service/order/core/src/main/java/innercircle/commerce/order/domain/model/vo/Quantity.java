package innercircle.commerce.order.domain.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 수량을 표현하는 값 객체
 */
public record Quantity(int value) {

    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 999;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Quantity {
        validate(value);
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    public Quantity subtract(Quantity other) {
        return new Quantity(this.value - other.value);
    }

    public Quantity increase() {
        return new Quantity(this.value + 1);
    }

    public Quantity decrease() {
        return new Quantity(this.value - 1);
    }

    private static void validate(int v) {
        if (v < MIN_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be at least " + MIN_QUANTITY);
        }
        if (v > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity cannot exceed " + MAX_QUANTITY);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
