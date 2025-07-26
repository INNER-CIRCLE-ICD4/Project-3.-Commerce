package innercircle.commerce.search.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductSearchRequest 테스트")
class ProductSearchRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("정상적인 요청 객체 생성")
    void createValidRequest() {
        // given & when
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("스마트폰")
                .categoryIds(List.of(1L, 2L))
                .brandIds(List.of(1L))
                .minPrice(new BigDecimal("100000"))
                .maxPrice(new BigDecimal("2000000"))
                .inStock(true)
                .status(ProductSearchRequest.ProductStatus.SELL)
                .sortType(ProductSearchRequest.SortType.PRICE_ASC)
                .page(0)
                .size(20)
                .build();

        // then
        assertThat(request.getKeyword()).isEqualTo("스마트폰");
        assertThat(request.getCategoryIds()).containsExactly(1L, 2L);
        assertThat(request.getBrandIds()).containsExactly(1L);
        assertThat(request.getMinPrice()).isEqualTo(new BigDecimal("100000"));
        assertThat(request.getMaxPrice()).isEqualTo(new BigDecimal("2000000"));
        assertThat(request.getInStock()).isTrue();
        assertThat(request.getStatus()).isEqualTo(ProductSearchRequest.ProductStatus.SELL);
        assertThat(request.getSortType()).isEqualTo(ProductSearchRequest.SortType.PRICE_ASC);
        assertThat(request.getPage()).isEqualTo(0);
        assertThat(request.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("기본값 설정 확인")
    void defaultValues() {
        // given & when
        ProductSearchRequest request = ProductSearchRequest.builder().build();

        // then
        assertThat(request.getSortType()).isEqualTo(ProductSearchRequest.SortType.RELEVANCE);
        assertThat(request.getPage()).isEqualTo(0);
        assertThat(request.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("유효성 검증 - 정상적인 요청")
    void validation_ValidRequest() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("정상적인검색어")
                .page(0)
                .size(10)
                .build();

        // when
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("유효성 검증 - 키워드 길이 초과")
    void validation_KeywordTooLong() {
        // given
        String longKeyword = "a".repeat(101); // 100자 초과
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(longKeyword)
                .build();

        // when
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProductSearchRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).contains("검색어는 1자 이상 100자 이하로 입력해주세요");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("keyword");
    }

    @Test
    @DisplayName("유효성 검증 - 빈 키워드")
    void validation_EmptyKeyword() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("")
                .build();

        // when
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProductSearchRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).contains("검색어는 1자 이상 100자 이하로 입력해주세요");
    }

    @Test
    @DisplayName("유효성 검증 - 페이지 번호 음수")
    void validation_NegativePage() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .page(-1)
                .build();

        // when
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProductSearchRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).contains("페이지 번호는 0 이상이어야 합니다");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("page");
    }

    @Test
    @DisplayName("유효성 검증 - 페이지 크기 0")
    void validation_ZeroSize() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .size(0)
                .build();

        // when
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProductSearchRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).contains("페이지 크기는 1 이상이어야 합니다");
    }

    @Test
    @DisplayName("유효성 검증 - 페이지 크기 초과")
    void validation_SizeTooLarge() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .size(101)
                .build();

        // when
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProductSearchRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).contains("페이지 크기는 100 이하여야 합니다");
    }

    @Test
    @DisplayName("ProductStatus Enum 테스트")
    void productStatusEnum() {
        // when & then
        assertThat(ProductSearchRequest.ProductStatus.SELL).isNotNull();
        assertThat(ProductSearchRequest.ProductStatus.SOLD_OUT).isNotNull();
        assertThat(ProductSearchRequest.ProductStatus.DISCONTINUED).isNotNull();
        
        assertThat(ProductSearchRequest.ProductStatus.values()).hasSize(3);
        assertThat(ProductSearchRequest.ProductStatus.valueOf("SELL"))
                .isEqualTo(ProductSearchRequest.ProductStatus.SELL);
    }

    @Test
    @DisplayName("SortType Enum 테스트")
    void sortTypeEnum() {
        // when & then
        assertThat(ProductSearchRequest.SortType.RELEVANCE).isNotNull();
        assertThat(ProductSearchRequest.SortType.PRICE_ASC).isNotNull();
        assertThat(ProductSearchRequest.SortType.PRICE_DESC).isNotNull();
        assertThat(ProductSearchRequest.SortType.NEWEST).isNotNull();
        assertThat(ProductSearchRequest.SortType.POPULAR).isNotNull();
        
        assertThat(ProductSearchRequest.SortType.values()).hasSize(5);
        assertThat(ProductSearchRequest.SortType.valueOf("RELEVANCE"))
                .isEqualTo(ProductSearchRequest.SortType.RELEVANCE);
    }

    @Test
    @DisplayName("Builder 패턴 테스트")
    void builderPattern() {
        // given & when
        ProductSearchRequest request1 = ProductSearchRequest.builder()
                .keyword("테스트")
                .build();

        ProductSearchRequest request2 = ProductSearchRequest.builder()
                .keyword("테스트")
                .page(1)
                .size(10)
                .build();

        // then
        assertThat(request1.getKeyword()).isEqualTo("테스트");
        assertThat(request1.getPage()).isEqualTo(0); // 기본값
        assertThat(request1.getSize()).isEqualTo(20); // 기본값

        assertThat(request2.getKeyword()).isEqualTo("테스트");
        assertThat(request2.getPage()).isEqualTo(1);
        assertThat(request2.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("null 값 처리")
    void nullValues() {
        // given & when
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(null)
                .categoryIds(null)
                .brandIds(null)
                .minPrice(null)
                .maxPrice(null)
                .inStock(null)
                .status(null)
                .build();

        // then
        assertThat(request.getKeyword()).isNull();
        assertThat(request.getCategoryIds()).isNull();
        assertThat(request.getBrandIds()).isNull();
        assertThat(request.getMinPrice()).isNull();
        assertThat(request.getMaxPrice()).isNull();
        assertThat(request.getInStock()).isNull();
        assertThat(request.getStatus()).isNull();
        
        // 기본값은 유지되어야 함
        assertThat(request.getSortType()).isEqualTo(ProductSearchRequest.SortType.RELEVANCE);
        assertThat(request.getPage()).isEqualTo(0);
        assertThat(request.getSize()).isEqualTo(20);
    }
}