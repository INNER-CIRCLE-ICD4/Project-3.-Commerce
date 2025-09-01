package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductAdminInfo;
import innercircle.commerce.product.admin.application.dto.ProductStockAdjustCommand;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductStockAdjustUseCaseTest {

	@InjectMocks
	private ProductStockAdjustUseCase useCase;

	@Mock
	private ProductRepository productRepository;

	@Nested
	@DisplayName("재고 조정")
	class AdjustStock {

		@Test
		@DisplayName("재고를 정상적으로 절대값으로 조정할 수 있다.")
		void 재고_조정_성공() {
			// given
			Long productId = 1L;
			Product product = createTestProduct(productId, 50, 1L);
			ProductStockAdjustCommand command = ProductStockAdjustCommand.of(productId, 100);

			given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
			given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			ProductAdminInfo result = useCase.adjustStock(command);

			// then
			assertThat(result.getStock()).isEqualTo(100);
		}

		@Test
		@DisplayName("재고를 0으로 조정할 수 있다.")
		void 재고_0으로_조정_성공() {
			// given
			Long productId = 1L;
			Product product = createTestProduct(productId, 50, 1L);
			ProductStockAdjustCommand command = ProductStockAdjustCommand.of(productId, 0);

			given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
			given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			ProductAdminInfo result = useCase.adjustStock(command);

			// then
			assertThat(result.getStock()).isEqualTo(0);
		}

		@Test
		@DisplayName("기존 재고보다 많은 수량으로 조정할 수 있다.")
		void 재고_증가_조정_성공() {
			// given
			Long productId = 1L;
			Product product = createTestProduct(productId, 10, 1L);
			ProductStockAdjustCommand command = ProductStockAdjustCommand.of(productId, 200);

			given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
			given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			ProductAdminInfo result = useCase.adjustStock(command);

			// then
			assertThat(result.getStock()).isEqualTo(200);
		}

		@Test
		@DisplayName("기존 재고보다 적은 수량으로 조정할 수 있다.")
		void 재고_감소_조정_성공() {
			// given
			Long productId = 1L;
			Product product = createTestProduct(productId, 100, 1L);
			ProductStockAdjustCommand command = ProductStockAdjustCommand.of(productId, 5);

			given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
			given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			ProductAdminInfo result = useCase.adjustStock(command);

			// then
			assertThat(result.getStock()).isEqualTo(5);
		}

		@Test
		@DisplayName("음수 수량으로 조정을 시도하면 예외가 발생한다.")
		void 재고_조정_음수_예외() {
			// given
			Long productId = 1L;
			Product product = createTestProduct(productId, 10, 1L);
			ProductStockAdjustCommand command = ProductStockAdjustCommand.of(productId, -5);

			given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));

			// when & then
			assertThatThrownBy(() -> useCase.adjustStock(command))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("조정할 재고 수량은 0 이상이어야 합니다.");
		}

		@Test
		@DisplayName("null 수량으로 조정을 시도하면 예외가 발생한다.")
		void 재고_조정_null_수량_예외() {
			// given
			Long productId = 1L;
			Product product = createTestProduct(productId, 10, 1L);
			ProductStockAdjustCommand command = ProductStockAdjustCommand.of(productId, null);

			given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));

			// when & then
			assertThatThrownBy(() -> useCase.adjustStock(command))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("조정할 재고 수량은 필수입니다.");
		}
	}

	@Nested
	@DisplayName("상품 조회")
	class ProductRetrieval {

		@Test
		@DisplayName("존재하지 않는 상품 ID로 재고 조정을 시도하면 예외가 발생한다.")
		void 상품_없음_예외() {
			// given
			Long productId = 999L;
			ProductStockAdjustCommand command = ProductStockAdjustCommand.of(productId, 50);

			given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> useCase.adjustStock(command))
					.isInstanceOf(ProductNotFoundException.class)
					.hasMessage(productId.toString());
		}
	}

	private Product createTestProduct(Long id, Integer stock, Long version) {
		LocalDateTime now = LocalDateTime.now();
		return Product.restore(
				id,
				"테스트 상품",
				1L,
				1L,
				10000,
				stock,
				version,
				null,
				null,
				"테스트 상품 설명",
				SaleType.NEW,
				ProductStatus.SALE,
				now,
				now
		);
	}
}