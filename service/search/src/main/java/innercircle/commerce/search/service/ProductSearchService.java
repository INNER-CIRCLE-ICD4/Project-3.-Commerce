package innercircle.commerce.search.service;

import innercircle.commerce.search.domain.Product;
import innercircle.commerce.search.dto.ProductBulkIndexRequest;
import innercircle.commerce.search.dto.ProductBulkIndexResponse;
import innercircle.commerce.search.dto.ProductRequest;
import innercircle.commerce.search.dto.ProductSearchRequest;
import innercircle.commerce.search.dto.ProductSearchResponse;
import innercircle.commerce.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {
    
    private final ProductSearchRepository productSearchRepository;
    
    public ProductSearchResponse searchProducts(ProductSearchRequest request) {
        log.info("상품 검색 요청: keyword={}, categoryIds={}, brandIds={}, minPrice={}, maxPrice={}", 
                request.getKeyword(), request.getCategoryIds(), request.getBrandIds(), 
                request.getMinPrice(), request.getMaxPrice());
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        
        try {
            // 일단 간단하게 findAll()로 테스트
            Page<Product> productPage = productSearchRepository.findAll(pageable);
            
            List<ProductSearchResponse.ProductDto> productDtos = productPage.getContent().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return ProductSearchResponse.builder()
                    .totalElements(productPage.getTotalElements())
                    .totalPages(productPage.getTotalPages())
                    .currentPage(productPage.getNumber())
                    .pageSize(productPage.getSize())
                    .products(productDtos)
                    .build();
        } catch (Exception e) {
            log.error("검색 중 오류 발생", e);
            throw e;
        }
    }
    
    public List<String> getAutocompleteSuggestions(String keyword, int size) {
        log.info("자동완성 요청: keyword={}, size={}", keyword, size);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        
        return productSearchRepository.getAutocompleteSuggestions(keyword, size);
    }
    
    public void indexProduct(Product product) {
        log.info("상품 인덱싱: productId={}", product.getId());
        productSearchRepository.save(product);
    }
    
    public void deleteProduct(String productId) {
        log.info("상품 삭제: productId={}", productId);
        productSearchRepository.deleteById(productId);
    }
    
    @Transactional
    public ProductBulkIndexResponse bulkIndexProducts(ProductBulkIndexRequest request) {
        log.info("벌크 상품 인덱싱 요청: {} 개 상품", request.getProducts().size());
        
        // Validation
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            log.warn("벌크 인덱싱 요청에 상품이 없습니다");
            return ProductBulkIndexResponse.failure("상품 목록이 비어있습니다");
        }
        
        // 상품 크기 제한 체크 (추가 검증)
        if (request.getProducts().size() > 1000) {
            log.warn("벌크 인덱싱 요청 상품 수가 제한을 초과합니다: {}", request.getProducts().size());
            return ProductBulkIndexResponse.failure("한 번에 최대 1000개의 상품만 인덱싱할 수 있습니다");
        }
        
        try {
            // ProductRequest를 Product 도메인 객체로 변환
            List<Product> products = request.getProducts().stream()
                    .map(ProductRequest::toDocument)
                    .collect(Collectors.toList());
            
            // 중복 ID 체크
            long uniqueIdCount = products.stream()
                    .map(Product::getId)
                    .distinct()
                    .count();
            
            if (uniqueIdCount < products.size()) {
                log.warn("중복된 상품 ID가 포함되어 있습니다. 전체: {}, 고유: {}", 
                        products.size(), uniqueIdCount);
            }
            
            // 벌크 인덱싱 실행
            ProductBulkIndexResponse response = productSearchRepository.bulkIndex(
                    products, 
                    request.isFailOnError()
            );
            
            // 결과 로깅
            if (response.getFailureCount() > 0) {
                log.warn("벌크 인덱싱 부분 실패: 성공 {}/{}, 실패 {}", 
                        response.getSuccessCount(), 
                        response.getTotalRequested(),
                        response.getFailureCount());
                
                // 실패한 항목 상세 로깅
                response.getResults().stream()
                        .filter(r -> !r.isSuccess())
                        .forEach(r -> log.error("상품 인덱싱 실패 - ID: {}, 오류: {}", 
                                r.getProductId(), r.getError()));
            } else {
                log.info("벌크 인덱싱 성공: {} 개 상품 ({}ms)", 
                        response.getSuccessCount(), response.getTookMillis());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("벌크 인덱싱 중 예외 발생", e);
            
            if (request.isFailOnError()) {
                throw new RuntimeException("벌크 인덱싱 실패: " + e.getMessage(), e);
            }
            
            return ProductBulkIndexResponse.failure("벌크 인덱싱 처리 중 오류 발생: " + e.getMessage());
        }
    }
    
    private ProductSearchResponse.ProductDto convertToDto(Product product) {
        return ProductSearchResponse.ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .detailContent(product.getDetailContent())
                .categories(product.getCategories())
                .status(product.getProductStatus())
                .code(product.getCode())
                .saleType(product.getSaleType())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}