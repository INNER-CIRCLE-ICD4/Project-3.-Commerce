package innercircle.commerce.search.controller;

import innercircle.commerce.search.dto.ProductSearchRequest;
import innercircle.commerce.search.dto.ProductSearchResponse;
import innercircle.commerce.search.service.ProductSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("자동완성 API 호출: keyword={}, size={}", keyword, size);
        List<String> suggestions = productSearchService.getAutocompleteSuggestions(keyword, size);
        return ResponseEntity.ok(suggestions);
    }
    
    @PostMapping("/products/index")
    public ResponseEntity<String> indexProduct(@RequestBody innercircle.commerce.search.domain.Product product) {
        log.info("상품 색인 API 호출: productId={}", product.getId());
        productSearchService.indexProduct(product);
        return ResponseEntity.ok("상품이 성공적으로 색인되었습니다.");
    }
}