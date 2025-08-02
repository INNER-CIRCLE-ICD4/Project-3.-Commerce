package innercircle.commerce.search.repository;

import innercircle.commerce.search.domain.Product;
import innercircle.commerce.search.dto.ProductSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        if (!criteriaList.isEmpty()) {
            Criteria combinedCriteria = criteriaList.get(0);
            for (int i = 1; i < criteriaList.size(); i++) {
                combinedCriteria = combinedCriteria.and(criteriaList.get(i));
            }
            query = new CriteriaQuery(combinedCriteria);
        } else {
            query = new CriteriaQuery(new Criteria());
        }
        
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
        
        Criteria criteria = new Criteria("name.autocomplete").contains(keyword);
        
        CriteriaQuery query = new CriteriaQuery(criteria);
        query.addFields("name");
        query.setMaxResults(size);
        
        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);
        
        return searchHits.stream()
                .map(hit -> hit.getContent().getName())
                .distinct()
                .limit(size)
                .collect(Collectors.toList());
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