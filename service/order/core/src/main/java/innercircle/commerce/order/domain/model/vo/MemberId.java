package innercircle.commerce.order.domain.model.vo;

import java.util.Objects;

/**
 * MemberId Value Object
 * 회원의 고유 식별자를 표현
 */
public class MemberId {
    private final Long value;

    private MemberId(Long value) {
        validate(value);
        this.value = value;
    }

    public static MemberId of(Long value) {
        return new MemberId(value);
    }
    
    public static MemberId of(String value) {
        return new MemberId(Long.parseLong(value));
    }

    private void validate(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("MemberId must be a positive number");
        }
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberId that = (MemberId) o;
        return Objects.equals(value, that.value);
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
