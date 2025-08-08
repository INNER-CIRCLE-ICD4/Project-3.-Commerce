package innercircle.commerce.search.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import innercircle.commerce.search.dto.ProductSearchRequest;
import innercircle.commerce.search.dto.ProductSearchResponse;
import innercircle.commerce.search.service.ProductSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductSearchController.class)
@DisplayName("ProductSearchController 테스트")
class ProductSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductSearchService productSearchService;

    private ProductSearchResponse mockResponse;

    @BeforeEach
    void setUp() {
        ProductSearchResponse.ProductDto productDto = ProductSearchResponse.ProductDto.builder()
                .id("1")
                .name("Nike 에어맥스 90 운동화")
                .detailContent("Nike의 클래식한 에어맥스 시리즈")
                .status("ACTIVE")
                .brandId(1L)
                .brandName("나이키")
                .build();

        mockResponse = ProductSearchResponse.builder()
                .totalElements(1L)
                .totalPages(1)
                .currentPage(0)
                .pageSize(20)
                .products(List.of(productDto))
                .build();
    }

    @Test
    @DisplayName("POST /api/search/products - 정상적인 검색 요청")
    void searchProducts_Success() throws Exception {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("스마트폰")
                .page(0)
                .size(20)
                .build();

        given(productSearchService.searchProducts(any(ProductSearchRequest.class)))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/search/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.products[0].name").value("Nike 에어맥스 90 운동화"))
                .andExpect(jsonPath("$.products[0].brandName").value("나이키"));

        verify(productSearchService).searchProducts(any(ProductSearchRequest.class));
    }

    @Test
    @DisplayName("POST /api/search/products - 잘못된 페이지 번호")
    void searchProducts_InvalidPage() throws Exception {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("스마트폰")
                .page(-1)  // 잘못된 페이지 번호
                .size(20)
                .build();

        // when & then
        mockMvc.perform(post("/api/search/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/search/products - 잘못된 페이지 크기")
    void searchProducts_InvalidSize() throws Exception {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("스마트폰")
                .page(0)
                .size(101)  // 최대값 초과
                .build();

        // when & then
        mockMvc.perform(post("/api/search/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/search/products - 너무 긴 검색어")
    void searchProducts_TooLongKeyword() throws Exception {
        // given
        String longKeyword = "a".repeat(101);  // 100자 초과
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(longKeyword)
                .page(0)
                .size(20)
                .build();

        // when & then
        mockMvc.perform(post("/api/search/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/search/products - 쿼리 파라미터로 검색")
    void searchProductsGet_Success() throws Exception {
        // given
        given(productSearchService.searchProducts(any(ProductSearchRequest.class)))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/search/products")
                        .param("keyword", "스마트폰")
                        .param("minPrice", "100000")
                        .param("maxPrice", "2000000")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.products[0].name").value("Nike 에어맥스 90 운동화"));

        verify(productSearchService).searchProducts(any(ProductSearchRequest.class));
    }

    @Test
    @DisplayName("GET /api/search/products - 카테고리 및 브랜드 ID로 필터링")
    void searchProductsGet_WithFilters() throws Exception {
        // given
        given(productSearchService.searchProducts(any(ProductSearchRequest.class)))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/search/products")
                        .param("keyword", "신발")
                        .param("categoryIds", "1", "2")
                        .param("brandIds", "1", "3")
                        .param("inStock", "true")
                        .param("sortType", "PRICE_ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productSearchService).searchProducts(any(ProductSearchRequest.class));
    }

    @Test
    @DisplayName("GET /api/search/autocomplete - 자동완성 요청")
    void getAutocompleteSuggestions_Success() throws Exception {
        // given
        List<String> suggestions = List.of("스마트폰", "스마트워치", "스마트TV");
        given(productSearchService.getAutocompleteSuggestions("스마트", 10))
                .willReturn(suggestions);

        // when & then
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("keyword", "스마트")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("스마트폰"))
                .andExpect(jsonPath("$[1]").value("스마트워치"))
                .andExpect(jsonPath("$[2]").value("스마트TV"));

        verify(productSearchService).getAutocompleteSuggestions("스마트", 10);
    }

    @Test
    @DisplayName("GET /api/search/autocomplete - 기본 사이즈로 자동완성")
    void getAutocompleteSuggestions_DefaultSize() throws Exception {
        // given
        List<String> suggestions = List.of("Nike");
        given(productSearchService.getAutocompleteSuggestions("Ni", 10))
                .willReturn(suggestions);

        // when & then
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("keyword", "Ni"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value("Nike"));

        verify(productSearchService).getAutocompleteSuggestions("Ni", 10);
    }

    @Test
    @DisplayName("POST /api/search/products/index - 상품 색인")
    void indexProduct_Success() throws Exception {
        // given
        String productJson = """
                {
                    "id": "1",
                    "name": "테스트 상품",
                    "description": "테스트 상품 설명",
                    "status": "ACTIVE",
                    "brandId": 1,
                    "brandName": "테스트 브랜드"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/search/products/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("상품이 성공적으로 색인되었습니다."));

        verify(productSearchService).indexProduct(any());
    }
}