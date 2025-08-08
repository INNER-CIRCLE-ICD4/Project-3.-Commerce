package innercircle.commerce.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBulkIndexResponse {
    
    private int totalRequested;
    private int successCount;
    private int failureCount;
    private long tookMillis;
    
    @Builder.Default
    private List<IndexResult> results = new ArrayList<>();
    
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndexResult {
        private String productId;
        private String productName;
        private boolean success;
        private String error;
        private String result; // CREATED, UPDATED, FAILED
    }
    
    public static ProductBulkIndexResponse success(int totalRequested, long tookMillis, List<IndexResult> results) {
        int successCount = (int) results.stream().filter(IndexResult::isSuccess).count();
        int failureCount = totalRequested - successCount;
        
        return ProductBulkIndexResponse.builder()
                .totalRequested(totalRequested)
                .successCount(successCount)
                .failureCount(failureCount)
                .tookMillis(tookMillis)
                .results(results)
                .build();
    }
    
    public static ProductBulkIndexResponse failure(String error) {
        return ProductBulkIndexResponse.builder()
                .totalRequested(0)
                .successCount(0)
                .failureCount(0)
                .tookMillis(0)
                .errors(List.of(error))
                .build();
    }
}