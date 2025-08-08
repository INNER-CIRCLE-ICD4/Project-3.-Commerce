package innercircle.commerce.search.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBulkIndexRequest {
    
    @NotEmpty(message = "상품 목록은 비어있을 수 없습니다")
    @Size(max = 1000, message = "한 번에 최대 1000개의 상품만 인덱싱할 수 있습니다")
    @Valid
    private List<ProductRequest> products;
    
    @Builder.Default
    private boolean failOnError = false; // true: 하나라도 실패하면 전체 롤백, false: 부분 실패 허용
}