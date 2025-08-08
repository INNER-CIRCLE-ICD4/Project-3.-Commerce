package innercircle.commerce.search.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import innercircle.commerce.search.dto.ProductBulkIndexRequest;
import innercircle.commerce.search.dto.ProductBulkIndexResponse;
import innercircle.commerce.search.dto.ProductRequest;
import innercircle.commerce.search.service.ProductSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductSearchController.class)
class ProductBulkIndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductSearchService productSearchService;

    private ProductBulkIndexRequest bulkIndexRequest;

    @BeforeEach
    void setUp() {
        List<ProductRequest> products = Arrays.asList(
                new ProductRequest(
                        "1",
                        "테스트 상품 1",
                        "TEST001",
                        "테스트 상품 1 상세 설명",
                        10000,
                        List.of("전자제품", "스마트폰"),
                        "ACTIVE",
                        "ONLINE"
                ),
                new ProductRequest(
                        "2",
                        "테스트 상품 2",
                        "TEST002",
                        "테스트 상품 2 상세 설명",
                        20000,
                        List.of("의류", "상의"),
                        "ACTIVE",
                        "ONLINE"
                ),
                new ProductRequest(
                        "3",
                        "테스트 상품 3",
                        "TEST003",
                        "테스트 상품 3 상세 설명",
                        30000,
                        List.of("식품", "스낵"),
                        "ACTIVE",
                        "ONLINE"
                )
        );

        bulkIndexRequest = ProductBulkIndexRequest.builder()
                .products(products)
                .failOnError(false)
                .build();
    }

    @Test
    @DisplayName("벌크 인덱싱 - 모든 상품 성공")
    void bulkIndexProducts_AllSuccess() throws Exception {
        // Given
        List<ProductBulkIndexResponse.IndexResult> results = Arrays.asList(
                ProductBulkIndexResponse.IndexResult.builder()
                        .productId("1")
                        .productName("테스트 상품 1")
                        .success(true)
                        .result("CREATED")
                        .build(),
                ProductBulkIndexResponse.IndexResult.builder()
                        .productId("2")
                        .productName("테스트 상품 2")
                        .success(true)
                        .result("CREATED")
                        .build(),
                ProductBulkIndexResponse.IndexResult.builder()
                        .productId("3")
                        .productName("테스트 상품 3")
                        .success(true)
                        .result("CREATED")
                        .build()
        );

        ProductBulkIndexResponse response = ProductBulkIndexResponse.success(3, 150L, results);

        when(productSearchService.bulkIndexProducts(any(ProductBulkIndexRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/search/products/bulk-index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkIndexRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequested").value(3))
                .andExpect(jsonPath("$.successCount").value(3))
                .andExpect(jsonPath("$.failureCount").value(0))
                .andExpect(jsonPath("$.tookMillis").value(150))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(3));
    }

    @Test
    @DisplayName("벌크 인덱싱 - 부분 성공")
    void bulkIndexProducts_PartialSuccess() throws Exception {
        // Given
        List<ProductBulkIndexResponse.IndexResult> results = Arrays.asList(
                ProductBulkIndexResponse.IndexResult.builder()
                        .productId("1")
                        .productName("테스트 상품 1")
                        .success(true)
                        .result("CREATED")
                        .build(),
                ProductBulkIndexResponse.IndexResult.builder()
                        .productId("2")
                        .productName("테스트 상품 2")
                        .success(false)
                        .result("FAILED")
                        .error("인덱싱 실패")
                        .build(),
                ProductBulkIndexResponse.IndexResult.builder()
                        .productId("3")
                        .productName("테스트 상품 3")
                        .success(true)
                        .result("CREATED")
                        .build()
        );

        ProductBulkIndexResponse response = ProductBulkIndexResponse.success(3, 200L, results);

        when(productSearchService.bulkIndexProducts(any(ProductBulkIndexRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/search/products/bulk-index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkIndexRequest)))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$.totalRequested").value(3))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.failureCount").value(1))
                .andExpect(jsonPath("$.results[1].success").value(false))
                .andExpect(jsonPath("$.results[1].error").value("인덱싱 실패"));
    }

    @Test
    @DisplayName("벌크 인덱싱 - 빈 상품 목록")
    void bulkIndexProducts_EmptyList() throws Exception {
        // Given
        ProductBulkIndexRequest emptyRequest = ProductBulkIndexRequest.builder()
                .products(List.of())
                .failOnError(false)
                .build();

        // When & Then
        mockMvc.perform(post("/api/search/products/bulk-index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("벌크 인덱싱 - 최대 개수 초과")
    void bulkIndexProducts_ExceedsMaxSize() throws Exception {
        // Given
        List<ProductRequest> tooManyProducts = new java.util.ArrayList<>();
        for (int i = 1; i <= 1001; i++) {
            tooManyProducts.add(new ProductRequest(
                    String.valueOf(i),
                    "상품 " + i,
                    "CODE" + i,
                    "상세 설명 " + i,
                    10000 * i,
                    List.of("카테고리"),
                    "ACTIVE",
                    "ONLINE"
            ));
        }

        ProductBulkIndexRequest oversizedRequest = ProductBulkIndexRequest.builder()
                .products(tooManyProducts)
                .failOnError(false)
                .build();

        ProductBulkIndexResponse response = ProductBulkIndexResponse.failure(
                "한 번에 최대 1000개의 상품만 인덱싱할 수 있습니다"
        );

        when(productSearchService.bulkIndexProducts(any(ProductBulkIndexRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/search/products/bulk-index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oversizedRequest)))
                .andExpect(status().isInternalServerError());
    }
}