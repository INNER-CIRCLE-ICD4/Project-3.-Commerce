package innercircle.commerce.search.repository;

import innercircle.commerce.search.domain.Product;
import innercircle.commerce.search.dto.ProductBulkIndexResponse;
import innercircle.commerce.search.dto.ProductSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductSearchRepositoryCustom {
    Page<Product> searchProducts(ProductSearchRequest request, Pageable pageable);
    List<String> getAutocompleteSuggestions(String keyword, int size);
    ProductBulkIndexResponse bulkIndex(List<Product> products, boolean failOnError);
}