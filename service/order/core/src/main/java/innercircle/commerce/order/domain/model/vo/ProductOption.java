package innercircle.commerce.order.domain.model.vo;

import java.util.Objects;

/**
 * ProductOption Value Object
 * 상품 옵션 정보
 */
public class ProductOption {
    private final Long optionId;
    private final String optionName;

    public ProductOption(Long optionId, String optionName) {
        this.optionId = optionId;
        this.optionName = optionName;
    }

    public static ProductOption of(Long optionId, String optionName) {
        return new ProductOption(optionId, optionName);
    }

    public Long getOptionId() {
        return optionId;
    }

    public String getOptionName() {
        return optionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductOption that = (ProductOption) o;
        return Objects.equals(optionId, that.optionId) && Objects.equals(optionName, that.optionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionId, optionName);
    }

    @Override
    public String toString() {
        return "ProductOption{" +
                "optionId=" + optionId +
                ", optionName='" + optionName + '\'' +
                '}';
    }
}
