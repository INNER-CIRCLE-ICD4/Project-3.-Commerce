package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.event.ProductDeletedEvent;
import innercircle.commerce.product.admin.fixtures.ProductFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 삭제 UseCase 테스트")
class ProductDeleteUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ProductDeleteUseCase productDeleteUseCase;

    @BeforeEach
    void setUp() {
        productDeleteUseCase = new ProductDeleteUseCase(productRepository, eventPublisher);
    }

    @Test
    @DisplayName("정상 상태의 상품을 삭제할 수 있다.")
    void 정상_상품_삭제_성공() {
        // given
        Long productId = 1L;
        Product existingProduct = ProductFixtures.createValidProductWithImages();
        Product deletedProduct = ProductFixtures.createDeletedProduct();

        given(productRepository.findById(productId)).willReturn(Optional.of(existingProduct));
        given(productRepository.save(any(Product.class))).willReturn(deletedProduct);

        // when
        productDeleteUseCase.deleteProduct(productId);

        // then
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
        
        // 이벤트 발행 검증
        ArgumentCaptor<ProductDeletedEvent> eventCaptor = ArgumentCaptor.forClass(ProductDeletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        ProductDeletedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getProductId()).isEqualTo(productId);
        assertThat(publishedEvent.getImageUrls()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 상품 삭제 시 예외가 발생한다.")
    void 존재하지_않는_상품_삭제_시_예외_발생() {
        // given
        Long nonExistentProductId = 999L;

        given(productRepository.findById(nonExistentProductId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productDeleteUseCase.deleteProduct(nonExistentProductId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(nonExistentProductId.toString());
    }

    @Test
    @DisplayName("이미 삭제된 상품을 다시 삭제하면 예외가 발생한다.")
    void 이미_삭제된_상품_삭제_시_예외_발생() {
        // given
        Long productId = 1L;
        Product alreadyDeletedProduct = ProductFixtures.createDeletedProduct();

        given(productRepository.findById(productId)).willReturn(Optional.of(alreadyDeletedProduct));

        // when & then
        assertThatThrownBy(() -> productDeleteUseCase.deleteProduct(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 삭제된 상품입니다.");
    }
}