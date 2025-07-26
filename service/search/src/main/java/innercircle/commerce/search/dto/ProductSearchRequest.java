package innercircle.commerce.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    
    @Size(min = 1, max = 100, message = "검색어는 1자 이상 100자 이하로 입력해주세요.")
    private String keyword;
    
    private List<Long> categoryIds;
    
    private List<Long> brandIds;
    
    private BigDecimal minPrice;
    
    private BigDecimal maxPrice;
    
    private Boolean inStock;
    
    private ProductStatus status;
    
    @Builder.Default
    private SortType sortType = SortType.RELEVANCE;
    
    @Builder.Default
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private Integer page = 0;
    
    @Builder.Default
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
    private Integer size = 20;
    
    public enum ProductStatus {
        SELL, SOLD_OUT, DISCONTINUED
    }
    
    public enum SortType {
        RELEVANCE,      // 관련도순
        PRICE_ASC,      // 가격 낮은순
        PRICE_DESC,     // 가격 높은순
        NEWEST,         // 최신순
        POPULAR         // 인기순
    }
}