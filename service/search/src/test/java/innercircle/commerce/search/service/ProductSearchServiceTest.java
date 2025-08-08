package innercircle.commerce.search.service;

import innercircle.commerce.search.domain.Product;
import innercircle.commerce.search.dto.ProductSearchRequest;
import innercircle.commerce.search.dto.ProductSearchResponse;
import innercircle.commerce.search.repository.ProductSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchService 테스트")
class ProductSearchServiceTest {

    @Mock
    private ProductSearchRepository productSearchRepository;

    @InjectMocks
    private ProductSearchService productSearchService;

    private Product mockProduct;
    private ProductSearchRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Mock Product 생성
        Product.Category category = Product.Category.builder()
                .id(1L)
                .name("신발")
                .code("SHOES")
                .depth(1)
                .parentId(null)
                .build();

        Product.Price price = Product.Price.builder()
                .originalPrice(new BigDecimal("159000"))
                .discountRate(new BigDecimal("10"))
                .isDiscount(true)
                .discountStartDate(LocalDateTime.now())
                .discountEndDate(LocalDateTime.now().plusDays(30))
                .finalPrice(new BigDecimal("143100"))
                .build();

        Product.Option option = Product.Option.builder()
                .id(1L)
                .groupName("사이즈")
                .optionName("260mm")
                .additionalPrice(new BigDecimal("0"))
                .stock(10)
                .build();

        mockProduct = Product.builder()
                .id("1")
                .name("Nike 에어맥스 90 운동화")
                .description("Nike의 클래식한 에어맥스 시리즈 스니커즈로 편안한 착용감을 제공합니다")
                .productStatus("ACTIVE")
                .brandId(1L)
                .brandName("나이키")
                .categories(List.of(category))
                .price(price)
                .options(List.of(option))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Mock Request 생성
        mockRequest = ProductSearchRequest.builder()
                .keyword("스마트폰")
                .page(0)
                .size(20)
                .sortType(ProductSearchRequest.SortType.RELEVANCE)
                .build();
    }

    @Test
    @DisplayName("상품 검색 - 정상 동작")
    void searchProducts_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> mockPage = new PageImpl<>(List.of(mockProduct), pageable, 1);
        
        given(productSearchRepository.findAll(any(Pageable.class)))
                .willReturn(mockPage);

        // when
        ProductSearchResponse response = productSearchService.searchProducts(mockRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1L);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(20);
        assertThat(response.getProducts()).hasSize(1);

        ProductSearchResponse.ProductDto productDto = response.getProducts().get(0);
        assertThat(productDto.getId()).isEqualTo("1");
        assertThat(productDto.getName()).isEqualTo("Nike 에어맥스 90 운동화");
        assertThat(productDto.getBrandName()).isEqualTo("나이키");
        assertThat(productDto.getCategories()).hasSize(1);
        assertThat(productDto.getPrice()).isNotNull();
        assertThat(productDto.getOptions()).hasSize(1);

        verify(productSearchRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("상품 검색 - 빈 결과")
    void searchProducts_EmptyResult() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        
        given(productSearchRepository.findAll(any(Pageable.class)))
                .willReturn(emptyPage);

        // when
        ProductSearchResponse response = productSearchService.searchProducts(mockRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(0L);
        assertThat(response.getProducts()).isEmpty();
    }

    @Test
    @DisplayName("상품 검색 - 예외 발생")
    void searchProducts_Exception() {
        // given
        given(productSearchRepository.findAll(any(Pageable.class)))
                .willThrow(new RuntimeException("Elasticsearch 연결 오류"));

        // when & then
        assertThatThrownBy(() -> productSearchService.searchProducts(mockRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Elasticsearch 연결 오류");
    }

    @Test
    @DisplayName("자동완성 조회 - 정상 동작")
    void getAutocompleteSuggestions_Success() {
        // given
        String keyword = "스마트";
        int size = 10;
        List<String> mockSuggestions = List.of("스마트폰", "스마트워치", "스마트TV");
        
        given(productSearchRepository.getAutocompleteSuggestions(keyword, size))
                .willReturn(mockSuggestions);

        // when
        List<String> suggestions = productSearchService.getAutocompleteSuggestions(keyword, size);

        // then
        assertThat(suggestions).hasSize(3);
        assertThat(suggestions).containsExactly("스마트폰", "스마트워치", "스마트TV");
        verify(productSearchRepository).getAutocompleteSuggestions(keyword, size);
    }

    @Test
    @DisplayName("자동완성 조회 - 빈 키워드")
    void getAutocompleteSuggestions_EmptyKeyword() {
        // when
        List<String> suggestions1 = productSearchService.getAutocompleteSuggestions(null, 10);
        List<String> suggestions2 = productSearchService.getAutocompleteSuggestions("", 10);
        List<String> suggestions3 = productSearchService.getAutocompleteSuggestions("   ", 10);

        // then
        assertThat(suggestions1).isEmpty();
        assertThat(suggestions2).isEmpty();
        assertThat(suggestions3).isEmpty();
    }

    @Test
    @DisplayName("상품 색인 - 정상 동작")
    void indexProduct_Success() {
        // given
        given(productSearchRepository.save(any(Product.class)))
                .willReturn(mockProduct);

        // when
        productSearchService.indexProduct(mockProduct);

        // then
        verify(productSearchRepository).save(mockProduct);
    }

    @Test
    @DisplayName("상품 삭제 - 정상 동작")
    void deleteProduct_Success() {
        // given
        String productId = "1";

        // when
        productSearchService.deleteProduct(productId);

        // then
        verify(productSearchRepository).deleteById(productId);
    }

    @Test
    @DisplayName("DTO 변환 - 카테고리가 null인 경우")
    void convertToDto_NullCategories() {
        // given
        Product productWithNullCategories = Product.builder()
                .id("1")
                .name("테스트 상품")
                .description("테스트 설명")
                .productStatus("ACTIVE")
                .brandId(1L)
                .brandName("테스트 브랜드")
                .categories(null)  // null 카테고리
                .price(null)       // null 가격
                .options(null)     // null 옵션
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> mockPage = new PageImpl<>(List.of(productWithNullCategories), pageable, 1);
        
        given(productSearchRepository.findAll(any(Pageable.class)))
                .willReturn(mockPage);

        // when
        ProductSearchResponse response = productSearchService.searchProducts(mockRequest);

        // then
        assertThat(response.getProducts()).hasSize(1);
        ProductSearchResponse.ProductDto productDto = response.getProducts().get(0);
        assertThat(productDto.getCategories()).isEmpty();
        assertThat(productDto.getPrice()).isNull();
        assertThat(productDto.getOptions()).isEmpty();
    }

    @Test
    @DisplayName("가격 DTO 변환 - null 처리")
    void convertPriceDto_NullPrice() {
        // given
        Product productWithNullPrice = Product.builder()
                .id("1")
                .name("테스트 상품")
                .price(null)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> mockPage = new PageImpl<>(List.of(productWithNullPrice), pageable, 1);
        
        given(productSearchRepository.findAll(any(Pageable.class)))
                .willReturn(mockPage);

        // when
        ProductSearchResponse response = productSearchService.searchProducts(mockRequest);

        // then
        ProductSearchResponse.ProductDto productDto = response.getProducts().get(0);
        assertThat(productDto.getPrice()).isNull();
    }
}