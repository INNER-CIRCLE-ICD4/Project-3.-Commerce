package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductAdminInfo;
import innercircle.commerce.product.admin.application.dto.ProductListAdminInfo;
import innercircle.commerce.product.admin.application.dto.ProductListQuery;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.admin.fixtures.ProductFixtures;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductRetrieveUseCase 테스트")
class ProductRetrieveUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductRetrieveUseCase productRetrieveUseCase;

    @Nested
    @DisplayName("상품 목록 조회")
    class GetProducts {

        @Test
        @DisplayName("전체 상품 목록을 조회할 수 있다.")
        void 전체_상품_목록_조회() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            ProductListQuery query = ProductListQuery.builder()
                    .pageable(pageable)
                    .build();
            
            List<Product> products = List.of(
                    ProductFixtures.createValidProduct(),
                    ProductFixtures.createValidProduct()
            );
            Page<Product> expectedPage = new PageImpl<>(products, pageable, products.size());
            
            given(productRepository.findProducts(null, null, pageable))
                    .willReturn(expectedPage);

            // when
            Page<ProductListAdminInfo> result = productRetrieveUseCase.getProducts(query);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(productRepository).findProducts(null, null, pageable);
        }

        @Test
        @DisplayName("상태별 상품 목록을 조회할 수 있다.")
        void 상태별_상품_목록_조회() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            ProductStatus status = ProductStatus.SALE;
            ProductListQuery query = ProductListQuery.builder()
                    .status(status)
                    .pageable(pageable)
                    .build();
            
            List<Product> products = List.of(ProductFixtures.createValidProduct());
            Page<Product> expectedPage = new PageImpl<>(products, pageable, products.size());
            
            given(productRepository.findProducts(status, null, pageable))
                    .willReturn(expectedPage);

            // when
            Page<ProductListAdminInfo> result = productRetrieveUseCase.getProducts(query);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findProducts(status, null, pageable);
        }

        @Test
        @DisplayName("카테고리별 상품 목록을 조회할 수 있다.")
        void 카테고리별_상품_목록_조회() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            Long categoryId = 1L;
            ProductListQuery query = ProductListQuery.builder()
                    .categoryId(categoryId)
                    .pageable(pageable)
                    .build();
            
            List<Product> products = List.of(ProductFixtures.createValidProduct());
            Page<Product> expectedPage = new PageImpl<>(products, pageable, products.size());
            
            given(productRepository.findProducts(null, categoryId, pageable))
                    .willReturn(expectedPage);

            // when
            Page<ProductListAdminInfo> result = productRetrieveUseCase.getProducts(query);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findProducts(null, categoryId, pageable);
        }

        @Test
        @DisplayName("상태와 카테고리 조건으로 상품 목록을 조회할 수 있다.")
        void 상태와_카테고리_조건_상품_목록_조회() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            ProductStatus status = ProductStatus.SALE;
            Long categoryId = 1L;
            ProductListQuery query = ProductListQuery.builder()
                    .status(status)
                    .categoryId(categoryId)
                    .pageable(pageable)
                    .build();
            
            List<Product> products = List.of(ProductFixtures.createValidProduct());
            Page<Product> expectedPage = new PageImpl<>(products, pageable, products.size());
            
            given(productRepository.findProducts(status, categoryId, pageable))
                    .willReturn(expectedPage);

            // when
            Page<ProductListAdminInfo> result = productRetrieveUseCase.getProducts(query);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findProducts(status, categoryId, pageable);
        }
    }

    @Nested
    @DisplayName("상품 상세 조회")
    class GetProduct {

        @Test
        @DisplayName("상품 ID로 상품 상세 정보를 조회할 수 있다.")
        void 상품_상세_조회_성공() {
            // given
            Long productId = 1L;
            Product product = ProductFixtures.createValidProduct();
            
            given(productRepository.findById(productId))
                    .willReturn(Optional.of(product));

            // when
            ProductAdminInfo result = productRetrieveUseCase.getProduct(productId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(product.getId());
            verify(productRepository).findById(productId);
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 조회 시 예외가 발생한다.")
        void 존재하지_않는_상품_조회_예외() {
            // given
            Long productId = 999L;
            
            given(productRepository.findById(productId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productRetrieveUseCase.getProduct(productId))
                    .isInstanceOf(ProductNotFoundException.class);
            
            verify(productRepository).findById(productId);
        }
    }
}