package innercircle.commerce.search.controller;

import innercircle.commerce.search.dto.*;
import innercircle.commerce.search.service.ProductSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class ProductSearchController {
    
    private final ProductSearchService productSearchService;
    
    @PostMapping("/products")
    public ResponseEntity<ProductSearchResponse> searchProducts(@Valid @RequestBody ProductSearchRequest request) {
        log.info("상품 검색 API 호출: {}", request);
        ProductSearchResponse response = productSearchService.searchProducts(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/products")
    public ResponseEntity<ProductSearchResponse> searchProductsGet(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> brandIds,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) ProductSearchRequest.ProductStatus status,
            @RequestParam(required = false, defaultValue = "RELEVANCE") ProductSearchRequest.SortType sortType,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .categoryIds(categoryIds)
                .brandIds(brandIds)
                .minPrice(minPrice != null ? new java.math.BigDecimal(minPrice) : null)
                .maxPrice(maxPrice != null ? new java.math.BigDecimal(maxPrice) : null)
                .inStock(inStock)
                .status(status)
                .sortType(sortType)
                .page(page)
                .size(size)
                .build();
        
        log.info("상품 검색 GET API 호출: {}", request);
        ProductSearchResponse response = productSearchService.searchProducts(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> getAutocompleteSuggestions(
            @RequestParam String name,
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("자동완성 API 호출: keyword={}, size={}", name, size);
        List<String> suggestions = productSearchService.getAutocompleteSuggestions(name, size);
        return ResponseEntity.ok(suggestions);
    }
    
    @PostMapping("/products/index")
    public ResponseEntity<String> indexProduct(@RequestBody ProductRequest request) {
        log.info("상품 색인 API 호출: productId={}", request.id());
        productSearchService.indexProduct(request.toDocument());
        return ResponseEntity.ok("상품이 성공적으로 색인되었습니다.");
    }
    
    @PostMapping("/products/bulk-index")
    public ResponseEntity<ProductBulkIndexResponse> bulkIndexProducts(
            @Valid @RequestBody ProductBulkIndexRequest request) {
        
        log.info("벌크 상품 색인 API 호출: {} 개 상품", request.getProducts().size());
        
        try {
            ProductBulkIndexResponse response = productSearchService.bulkIndexProducts(request);
            
            // 응답 상태 결정
            if (response.getFailureCount() == 0) {
                // 모든 상품이 성공적으로 인덱싱됨
                log.info("벌크 인덱싱 완료: 모든 상품 성공 ({} 개)", response.getSuccessCount());
                return ResponseEntity.ok(response);
            } else if (response.getSuccessCount() > 0) {
                // 부분 성공
                log.warn("벌크 인덱싱 부분 성공: 성공 {}/{}", 
                        response.getSuccessCount(), response.getTotalRequested());
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
            } else {
                // 모든 상품 실패
                log.error("벌크 인덱싱 실패: 모든 상품 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            log.error("벌크 인덱싱 API 처리 중 오류 발생", e);
            
            ProductBulkIndexResponse errorResponse = ProductBulkIndexResponse.failure(
                    "벌크 인덱싱 처리 중 오류 발생: " + e.getMessage()
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable String productId) {
        log.info("상품 삭제 API 호출: productId={}", productId);
        
        try {
            productSearchService.deleteProduct(productId);
            return ResponseEntity.ok("상품이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생: productId={}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}