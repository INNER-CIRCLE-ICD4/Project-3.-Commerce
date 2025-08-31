package innercircle.commerce.order.domain.model.vo;

import java.util.Objects;

/**
 * ProductId Value Object
 * 상품의 고유 식별자
 */
public class ProductId {
    private final Long value;

    private ProductId(Long value) {
        validate(value);
        this.value = value;
    }

    public static ProductId of(Long value) {
        return new ProductId(value);
    }
    
    public static ProductId of(String value) {
        return new ProductId(Long.parseLong(value));
    }

    private void validate(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductId must be a positive number");
        }
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductId productId = (ProductId) o;
        return Objects.equals(value, productId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
