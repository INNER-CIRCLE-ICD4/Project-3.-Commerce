package innercircle.commerce.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        private String detailContent;
        private String status;
        private String saleType;
        private String code;
        private List<String> categories;
        private Integer price;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}