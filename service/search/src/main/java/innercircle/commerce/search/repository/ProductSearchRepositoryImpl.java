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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductSearchRepositoryImpl implements ProductSearchRepositoryCustom {
    
    private final ElasticsearchOperations elasticsearchOperations;
    
    @Override
    public Page<Product> searchProducts(ProductSearchRequest request, Pageable pageable) {
        // 모든 문서를 검색하는 간단한 쿼리
        CriteriaQuery query = new CriteriaQuery(new Criteria());
        query.setPageable(pageable);
        
        // 검색 실행
        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);
        
        List<Product> products = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        
        return new PageImpl<>(products, pageable, searchHits.getTotalHits());
    }
    
    @Override
    public List<String> getAutocompleteSuggestions(String keyword, int size) {
        Criteria criteria = new Criteria("name.autocomplete").contains(keyword);
        
        CriteriaQuery query = new CriteriaQuery(criteria);
        query.addFields("name");
        query.setMaxResults(size);
        
        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);
        
        return searchHits.stream()
                .map(hit -> hit.getContent().getName())
                .distinct()
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