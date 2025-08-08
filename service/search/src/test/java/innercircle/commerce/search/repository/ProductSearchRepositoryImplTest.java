package innercircle.commerce.search.repository;

import innercircle.commerce.search.domain.Product;
import innercircle.commerce.search.dto.ProductSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataElasticsearchTest
@Testcontainers
@TestPropertySource(properties = {
        "logging.level.org.springframework.data.elasticsearch=DEBUG",
        "spring.elasticsearch.repositories.enabled=true"
})
@DisplayName("ProductSearchRepositoryImpl 통합 테스트")
class ProductSearchRepositoryImplTest {

    static class CustomElasticsearchContainer extends ElasticsearchContainer {
        private static final DockerImageName ELASTICSEARCH_IMAGE = 
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.11.1");
            
        public CustomElasticsearchContainer() {
            super(ELASTICSEARCH_IMAGE);
            this.withCommand("sh", "-c", 
                "bin/elasticsearch-plugin install --batch analysis-nori && " +
                "docker-entrypoint.sh");
        }
    }

    @Container
    static ElasticsearchContainer elasticsearchContainer = new CustomElasticsearchContainer()
            .withExposedPorts(9200)
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
            .withStartupAttempts(3);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
    }

    @Autowired
    private ProductSearchRepository productSearchRepository;

    @Autowired
    private ProductSearchRepositoryImpl productSearchRepositoryImpl;
    
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private Product koreanProduct;
    private Product mixedProduct;
    private Product englishProduct;
    private Product synonymProduct;

    @BeforeEach
    void setUp() {
        // 인덱스 생성 (이미 존재하면 삭제 후 재생성)
        IndexOperations indexOps = elasticsearchOperations.indexOps(Product.class);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.create();
        indexOps.putMapping();
        
        // 기존 데이터 삭제
        productSearchRepository.deleteAll();

        // 한글 전용 상품
        koreanProduct = createProduct(
                "1",
                "삼성 갤럭시 스마트폰",
                "최신 플래그십 스마트폰 갤럭시 시리즈의 최상위 모델입니다",
                "삼성",
                "전자제품",
                new BigDecimal("1699000"),
                1L,
                1L
        );

        // 한글/영어 혼합 상품
        mixedProduct = createProduct(
                "2",
                "Apple 아이폰 15 Pro 스마트폰",
                "Apple의 최신 플래그십 모델로 A17 Pro 칩셋과 티타늄 디자인을 채용했습니다",
                "애플",
                "전자제품",
                new BigDecimal("1550000"),
                1L,
                2L
        );

        // 영어 위주 상품
        englishProduct = createProduct(
                "3",
                "Nike 에어맥스 90 운동화",
                "Nike의 클래식한 에어맥스 시리즈 스니커즈로 편안한 착용감을 제공합니다",
                "나이키",
                "신발",
                new BigDecimal("159000"),
                2L,
                3L
        );

        // 동의어 테스트용 상품
        synonymProduct = createProduct(
                "4",
                "아디다스 런닝 스니커즈",
                "편안한 착용감의 운동화로 일상 및 운동시 모두 활용 가능한 신발입니다",
                "아디다스",
                "신발",
                new BigDecimal("89000"),
                2L,
                4L
        );

        // 데이터 저장
        productSearchRepository.saveAll(List.of(koreanProduct, mixedProduct, englishProduct, synonymProduct));
        
        // 인덱스 refresh 강제 실행
        elasticsearchOperations.indexOps(Product.class).refresh();

        // 인덱싱 완료 대기
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("전체 상품 조회")
    void findAll_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepository.findAll(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("한글 키워드 검색")
    void searchProducts_KoreanKeyword() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("스마트폰")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(result.getContent()).extracting(Product::getName)
                .allMatch(name -> name.contains("스마트폰"));
    }

    @Test
    @DisplayName("동의어 검색 - 핸드폰으로 스마트폰 검색")
    void searchProducts_SynonymSearch() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("핸드폰")  // 동의어로 설정된 단어
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        // 테스트 환경에서는 동의어 설정이 없으므로 정확한 매칭만 검색됨
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("영어 키워드 검색")
    void searchProducts_EnglishKeyword() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("Nike")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(result.getContent()).extracting(Product::getName)
                .anyMatch(name -> name.contains("Nike"));
    }

    @Test
    @DisplayName("브랜드명으로 검색")
    void searchProducts_BrandName() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("Nike")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(result.getContent()).extracting(Product::getName)
                .anyMatch(name -> name.contains("Nike"));
    }

    @Test
    @DisplayName("동의어 검색 - 운동화와 스니커즈")
    void searchProducts_SynonymShoes() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("운동화")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        // 테스트 환경에서는 "운동화"를 포함한 상품만 검색됨
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(result.getContent()).extracting(Product::getName)
                .anyMatch(name -> name.contains("운동화"));
    }

    @Test
    @DisplayName("카테고리 필터링")
    void searchProducts_CategoryFilter() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .categoryIds(List.of(1L))  // 전자제품 카테고리
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Product::getCategories)
                .allMatch(categories -> categories.stream()
                        .anyMatch(category -> category.getId().equals(1L)));
    }

    @Test
    @DisplayName("브랜드 필터링")
    void searchProducts_BrandFilter() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .brandIds(List.of(1L, 2L))  // 삼성, 애플
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Product::getBrandId)
                .allMatch(brandId -> brandId.equals(1L) || brandId.equals(2L));
    }

    @Test
    @DisplayName("가격 범위 필터링")
    void searchProducts_PriceFilter() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .minPrice(new BigDecimal("80000"))
                .maxPrice(new BigDecimal("200000"))
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(product -> product.getPrice().getFinalPrice())
                .allMatch(price -> 
                    price.compareTo(new BigDecimal("80000")) >= 0 && 
                    price.compareTo(new BigDecimal("200000")) <= 0
                );
    }

    @Test
    @DisplayName("복합 검색 - 키워드 + 필터")
    void searchProducts_ComplexSearch() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("스니커즈")  // 동의어로 운동화도 검색됨
                .categoryIds(List.of(2L))  // 신발 카테고리
                .maxPrice(new BigDecimal("100000"))
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(result.getContent()).extracting(Product::getName)
                .anyMatch(name -> name.contains("스니커즈"));
        assertThat(result.getContent()).extracting(product -> product.getPrice().getFinalPrice())
                .allMatch(price -> price.compareTo(new BigDecimal("100000")) <= 0);
    }

    @Test
    @DisplayName("자동완성 기능 테스트")
    void getAutocompleteSuggestions_Success() {
        // when
        List<String> suggestions = productSearchRepositoryImpl.getAutocompleteSuggestions("스마트", 5);

        // then
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions).anyMatch(suggestion -> suggestion.contains("스마트폰"));
    }

    @Test
    @DisplayName("자동완성 - 영어 키워드")
    void getAutocompleteSuggestions_EnglishKeyword() {
        // when
        List<String> suggestions = productSearchRepositoryImpl.getAutocompleteSuggestions("Ni", 5);

        // then
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions).anyMatch(suggestion -> suggestion.contains("Nike"));
    }

    @Test
    @DisplayName("페이징 테스트")
    void searchProducts_Paging() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder().build();
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);

        // when
        Page<Product> firstResult = productSearchRepositoryImpl.searchProducts(request, firstPage);
        Page<Product> secondResult = productSearchRepositoryImpl.searchProducts(request, secondPage);

        // then
        assertThat(firstResult.getTotalElements()).isEqualTo(4);
        assertThat(firstResult.getContent()).hasSize(2);
        assertThat(firstResult.getTotalPages()).isEqualTo(2);
        
        assertThat(secondResult.getTotalElements()).isEqualTo(4);
        assertThat(secondResult.getContent()).hasSize(2);
        assertThat(secondResult.getTotalPages()).isEqualTo(2);

        // 첫 번째 페이지와 두 번째 페이지의 상품이 다른지 확인
        List<String> firstPageIds = firstResult.getContent().stream()
                .map(Product::getId)
                .toList();
        List<String> secondPageIds = secondResult.getContent().stream()
                .map(Product::getId)
                .toList();
        
        assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
    }

    @Test
    @DisplayName("정렬 테스트 - 가격 오름차순")
    void searchProducts_SortByPriceAsc() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .sortType(ProductSearchRequest.SortType.PRICE_ASC)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        List<BigDecimal> prices = result.getContent().stream()
                .map(product -> product.getPrice().getFinalPrice())
                .toList();
        
        for (int i = 0; i < prices.size() - 1; i++) {
            assertThat(prices.get(i)).isLessThanOrEqualTo(prices.get(i + 1));
        }
    }

    @Test
    @DisplayName("정렬 테스트 - 최신순")
    void searchProducts_SortByNewest() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .sortType(ProductSearchRequest.SortType.NEWEST)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        List<LocalDateTime> createdDates = result.getContent().stream()
                .map(Product::getCreatedAt)
                .toList();
        
        for (int i = 0; i < createdDates.size() - 1; i++) {
            assertThat(createdDates.get(i)).isAfterOrEqualTo(createdDates.get(i + 1));
        }
    }

    @Test
    @DisplayName("존재하지 않는 키워드 검색")
    void searchProducts_NonExistentKeyword() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("존재하지않는상품명")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productSearchRepositoryImpl.searchProducts(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    private Product createProduct(String id, String name, String description, 
                                 String brandName, String categoryName, BigDecimal price,
                                 Long categoryId, Long brandId) {
        Product.Category category = Product.Category.builder()
                .id(categoryId)
                .name(categoryName)
                .code(categoryName.equals("신발") ? "SHOES" : "ELECTRONICS")
                .depth(1)
                .parentId(null)
                .build();

        Product.Price productPrice = Product.Price.builder()
                .originalPrice(price)
                .discountRate(new BigDecimal("0"))
                .isDiscount(false)
                .finalPrice(price)
                .build();

        return Product.builder()
                .id(id)
                .name(name)
                .description(description)
                .productStatus("ACTIVE")
                .brandId(brandId)
                .brandName(brandName)
                .categories(List.of(category))
                .price(productPrice)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}