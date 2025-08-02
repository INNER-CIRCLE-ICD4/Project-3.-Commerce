package innercircle.commerce.search.repository;

import innercircle.commerce.search.domain.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<Product, String>, ProductSearchRepositoryCustom {
}