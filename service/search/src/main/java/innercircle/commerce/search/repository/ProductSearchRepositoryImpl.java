package innercircle.commerce.search.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import innercircle.commerce.search.domain.Product;
import innercircle.commerce.search.dto.ProductBulkIndexResponse;
import innercircle.commerce.search.dto.ProductSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.BulkFailureException;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.elasticsearch.client.elc.NativeQuery.builder;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductSearchRepositoryImpl implements ProductSearchRepositoryCustom {
    
    private final ElasticsearchOperations elasticsearchOperations;
    
    @Override
    public Page<Product> searchProducts(ProductSearchRequest request, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        
        // 키워드 검색
        if (StringUtils.hasText(request.getKeyword())) {
            // Elasticsearch는 텍스트 필드를 토큰화하므로 is() 메서드를 사용하여 토큰 매칭
            Criteria keywordCriteria = new Criteria("name").is(request.getKeyword()).boost(3.0f)
                .or(new Criteria("description").is(request.getKeyword()).boost(2.0f))
                .or(new Criteria("brandName").is(request.getKeyword()).boost(2.0f))
                .or(new Criteria("categories.name").is(request.getKeyword()));
            criteriaList.add(keywordCriteria);
        }
        
        // 상태 필터 (활성 상품만)
        criteriaList.add(new Criteria("status").is("ACTIVE"));
        
        // 카테고리 필터
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            criteriaList.add(new Criteria("categories.id").in(request.getCategoryIds()));
        }
        
        // 브랜드 필터
        if (request.getBrandIds() != null && !request.getBrandIds().isEmpty()) {
            criteriaList.add(new Criteria("brandId").in(request.getBrandIds()));
        }
        
        // 가격 필터
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            Criteria priceCriteria = new Criteria("price.finalPrice");
            if (request.getMinPrice() != null) {
                priceCriteria = priceCriteria.greaterThanEqual(request.getMinPrice().doubleValue());
            }
            if (request.getMaxPrice() != null) {
                priceCriteria = priceCriteria.lessThanEqual(request.getMaxPrice().doubleValue());
            }
            criteriaList.add(priceCriteria);
        }
        
        // CriteriaQuery 생성
        CriteriaQuery query;
        Criteria combinedCriteria = criteriaList.getFirst();
        for (int i = 1; i < criteriaList.size(); i++) {
            combinedCriteria = combinedCriteria.and(criteriaList.get(i));
        }
        query = new CriteriaQuery(combinedCriteria);

        query.setPageable(pageable);
        
        // 정렬 적용
        applySorting(query, request.getSortType());
        
        // 검색 실행
        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);
        
        List<Product> products = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        
        return new PageImpl<>(products, pageable, searchHits.getTotalHits());
    }
    
    @Override
    public List<String> getAutocompleteSuggestions(String keyword, int size) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        Query multiMatchQuery = MultiMatchQuery.of(m -> m
                        .query(keyword)
                        .type(TextQueryType.BoolPrefix)
                        .fields("name.auto_complete",
                                "name.auto_complete._2gram",
                                "name.auto_complete._3gram")
                )
                ._toQuery();
        NativeQuery nativeQuery = builder()
                .withQuery(multiMatchQuery)
                .withPageable(PageRequest.of(0, size))
                .build();
        SearchHits<Product> searchHits = this.elasticsearchOperations.search(nativeQuery, Product.class);
        return searchHits.getSearchHits().stream()
                .map(hit -> {
                    Product product = hit.getContent();
                    return product.getName();
                })
                .toList();
    }
    
    @Override
    public ProductBulkIndexResponse bulkIndex(List<Product> products, boolean failOnError) {
        long startTime = System.currentTimeMillis();
        List<ProductBulkIndexResponse.IndexResult> results = new ArrayList<>();
        
        if (products == null || products.isEmpty()) {
            return ProductBulkIndexResponse.failure("상품 목록이 비어있습니다");
        }
        
        log.info("벌크 인덱싱 시작: {} 개 상품", products.size());
        
        try {
            // ElasticsearchOperations의 bulkIndex 사용
            List<IndexQuery> indexQueries = products.stream()
                    .map(product -> new IndexQueryBuilder()
                            .withId(product.getId())
                            .withObject(product)
                            .build())
                    .collect(Collectors.toList());
            
            // 벌크 인덱싱 실행
            List<IndexedObjectInformation> indexResults = elasticsearchOperations.bulkIndex(indexQueries, Product.class);
            
            // 인덱싱된 ID 목록 추출
            List<String> indexedIds = indexResults.stream()
                    .map(IndexedObjectInformation::id)
                    .collect(Collectors.toList());
            
            // 성공한 항목 처리
            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                boolean indexed = i < indexedIds.size() && indexedIds.get(i) != null;
                
                results.add(ProductBulkIndexResponse.IndexResult.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .success(indexed)
                        .result(indexed ? "CREATED" : "FAILED")
                        .error(indexed ? null : "인덱싱 실패")
                        .build());
            }
            
            long tookMillis = System.currentTimeMillis() - startTime;
            log.info("벌크 인덱싱 완료: 성공 {}/{} ({}ms)", 
                    indexedIds.size(), products.size(), tookMillis);
            
            return ProductBulkIndexResponse.success(products.size(), tookMillis, results);
            
        } catch (BulkFailureException e) {
            // 부분 실패 처리
            log.error("벌크 인덱싱 부분 실패: {}", e.getMessage());
            
            if (failOnError) {
                throw new RuntimeException("벌크 인덱싱 실패: " + e.getMessage(), e);
            }
            
            // 실패한 항목 처리
            e.getFailedDocuments().forEach((id, failure) -> {
                results.add(ProductBulkIndexResponse.IndexResult.builder()
                        .productId(id)
                        .success(false)
                        .result("FAILED")
                        .error(failure.toString())
                        .build());
            });
            
            // 성공한 항목 확인
            for (Product product : products) {
                boolean alreadyInResults = results.stream()
                        .anyMatch(r -> r.getProductId().equals(product.getId()));
                
                if (!alreadyInResults) {
                    results.add(ProductBulkIndexResponse.IndexResult.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .success(true)
                            .result("CREATED")
                            .build());
                }
            }
            
            long tookMillis = System.currentTimeMillis() - startTime;
            return ProductBulkIndexResponse.success(products.size(), tookMillis, results);
            
        } catch (Exception e) {
            log.error("벌크 인덱싱 중 예상치 못한 오류 발생", e);
            
            if (failOnError) {
                throw new RuntimeException("벌크 인덱싱 실패: " + e.getMessage(), e);
            }
            
            // 모든 항목 실패로 표시
            for (Product product : products) {
                results.add(ProductBulkIndexResponse.IndexResult.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .success(false)
                        .result("FAILED")
                        .error(e.getMessage())
                        .build());
            }
            
            long tookMillis = System.currentTimeMillis() - startTime;
            return ProductBulkIndexResponse.success(products.size(), tookMillis, results);
        }
    }
    
    private void applySorting(CriteriaQuery query, ProductSearchRequest.SortType sortType) {
        if (sortType == null) {
            return;
        }
        
        switch (sortType) {
            case PRICE_ASC:
                query.addSort(Sort.by(Sort.Direction.ASC, "price.finalPrice"));
                break;
            case PRICE_DESC:
                query.addSort(Sort.by(Sort.Direction.DESC, "price.finalPrice"));
                break;
            case NEWEST:
                query.addSort(Sort.by(Sort.Direction.DESC, "createdAt"));
                break;
            case POPULAR:
                // 인기순은 추후 조회수나 판매량 필드 추가 후 구현
                break;
            case RELEVANCE:
            default:
                // 기본은 관련도순 (ElasticSearch 기본 정렬)
                break;
        }
    }
}