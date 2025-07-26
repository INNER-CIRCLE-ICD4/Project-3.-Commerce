package innercircle.commerce.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {
    
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    private List<ProductDto> products;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDto {
        private String id;
        private String name;
        private String description;
        private String status;
        private Long brandId;
        private String brandName;
        private List<CategoryDto> categories;
        private PriceDto price;
        private List<OptionDto> options;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private Long id;
        private String name;
        private String code;
        private Integer depth;
        private Long parentId;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceDto {
        private BigDecimal originalPrice;
        private BigDecimal discountRate;
        private Boolean isDiscount;
        private LocalDateTime discountStartDate;
        private LocalDateTime discountEndDate;
        private BigDecimal finalPrice;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {
        private Long id;
        private String groupName;
        private String optionName;
        private BigDecimal additionalPrice;
        private Integer stock;
    }
}