package innercircle.commerce.order.domain.model.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Money Value Object
 * 금액을 표현하는 값 객체
 */
public class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("KRW");
    private static final int SCALE = 2;

    public Money(BigDecimal value, Currency currency) {
        validate(value);
        this.amount = value.setScale(SCALE, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public static Money of(BigDecimal value) {
        return new Money(value, DEFAULT_CURRENCY);
    }

    public static Money of(BigDecimal value, Currency currency) {
        return new Money(value, currency);
    }

    public static Money ofWon(long value) {
        return new Money(BigDecimal.valueOf(value), DEFAULT_CURRENCY);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, DEFAULT_CURRENCY);
    }

    /**
     * 금액 더하기
     */
    public Money add(Money other) {
        validateCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * 금액 빼기
     */
    public Money subtract(Money other) {
        validateCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        return new Money(result, this.currency);
    }

    /**
     * 금액 곱하기
     */
    public Money multiply(int multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("Multiplier cannot be negative");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    /**
     * 할인 적용
     */
    public Money applyDiscountRate(BigDecimal discountRate) {
        if (discountRate.compareTo(BigDecimal.ZERO) < 0 || 
            discountRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Discount rate must be between 0 and 1");
        }
        
        BigDecimal discountAmount = this.amount.multiply(discountRate);
        return new Money(this.amount.subtract(discountAmount), this.currency);
    }

    private void validate(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Money value cannot be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money value cannot be negative");
        }
    }

    private void validateCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot operate on different currencies");
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public boolean isGreaterThan(Money other) {
        validateCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        validateCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) &&
               Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return String.format("%s %s", amount, currency.getCurrencyCode());
    }
}
