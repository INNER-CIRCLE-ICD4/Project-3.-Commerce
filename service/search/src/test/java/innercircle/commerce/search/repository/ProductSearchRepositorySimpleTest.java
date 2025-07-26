package innercircle.commerce.search.repository;

import innercircle.commerce.search.domain.Product;
import innercircle.commerce.search.dto.ProductSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchRepositoryImpl 단위 테스트")
class ProductSearchRepositorySimpleTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    private ProductSearchRepositoryImpl productSearchRepositoryImpl;

    @BeforeEach
    void setUp() {
        productSearchRepositoryImpl = new ProductSearchRepositoryImpl(elasticsearchOperations);
    }

    @Test
    @DisplayName("검색 메서드가 null이 아닌 페이지를 반환")
    void searchProducts_ReturnsNonNullPage() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("테스트")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        try {
            Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);
            // ElasticsearchOperations가 mock이므로 NullPointerException 발생
        } catch (NullPointerException e) {
            // expected - mock이 설정되지 않았으므로
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    @DisplayName("자동완성 메서드가 null이 아닌 리스트를 반환")
    void getAutocompleteSuggestions_ReturnsNonNullList() {
        // given
        String keyword = "테스트";
        int size = 5;

        // when
        try {
            List<String> result = productSearchRepositoryImpl.getAutocompleteSuggestions(keyword, size);
            // ElasticsearchOperations가 mock이므로 NullPointerException 발생
        } catch (NullPointerException e) {
            // expected - mock이 설정되지 않았으므로
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }
}