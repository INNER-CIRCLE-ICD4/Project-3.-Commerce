package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductInventoryUpdateCommand;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.admin.application.exception.StockConflictException;
import innercircle.commerce.product.admin.web.dto.StockOperationType;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import jakarta.persistence.OptimisticLockException;
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
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class ProductInventoryUpdateUseCaseTest {

	@InjectMocks
	private ProductInventoryUpdateUseCase useCase;

	@Mock
	private ProductRepository productRepository;

	@Nested
	@DisplayName("재고 증가")
	class IncreaseStock {

		@Test
		@DisplayName("재고를 정상적으로 증가시킬 수 있다.")
		void 재고_증가_성공() {
			// given
			Long productId = 1L;
			Product product = createTestProduct(productId, 10, 1L);
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.INCREASE, 5);

			given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
			given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			Product result = useCase.updateStock(command);

			// then
			assertThat(result.getStock()).isEqualTo(15);
		}

		@Test
		@DisplayName("0 이하의 수량으로 재고 증가를 시도하면 예외가 발생한다.")
		void 재고_증가_수량_0이하_예외() {
			// given
			Long productId = 1L;
			Product product = createTestProduct(productId, 10, 1L);
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.INCREASE, 0);

			given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));

			// when & then
			assertThatThrownBy(() -> useCase.updateStock(command))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("증가할 재고 수량은 양수여야 합니다.");
		}
	}

	@Nested
	@DisplayName("재고 감소")
	class DecreaseStock {

		@Test
		@DisplayName("재고를 정상적으로 감소시킬 수 있다.")
		void 재고_감소_성공() {
			// given
			Long productId = 1L;
			Product product = createTestProduct(productId, 10, 1L);
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.DECREASE, 3);

			given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
			given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			Product result = useCase.updateStock(command);

			// then
			assertThat(result.getStock()).isEqualTo(7);
		}

		@Test
		@DisplayName("재고보다 많은 수량을 감소시키려 하면 예외가 발생한다.")
		void 재고_감소_부족_예외() {
			// given
			Long productId = 1L;
			Product product = createTestProduct(productId, 5, 1L);
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.DECREASE, 10);

			given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));

			// when & then
			assertThatThrownBy(() -> useCase.updateStock(command))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("재고가 부족합니다. 현재 재고: 5, 요청 수량: 10");
		}
	}

	@Nested
	@DisplayName("낙관적 락 충돌 처리")
	class OptimisticLockHandling {

		@Test
		@DisplayName("OptimisticLockException이 발생하면 StockConflictException으로 변환된다.")
		void 낙관적_락_충돌_예외_변환() {
			// given
			Long productId = 1L;
			Product product = createTestProduct(productId, 10, 1L);
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.INCREASE, 5);

			given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
			willThrow(new OptimisticLockException()).given(productRepository).save(any(Product.class));

			// when & then
			assertThatThrownBy(() -> useCase.updateStock(command))
					.isInstanceOf(StockConflictException.class)
					.hasMessage("재고 변경 중 충돌이 발생했습니다. 잠시 후 다시 시도해주세요.")
					.hasCauseInstanceOf(OptimisticLockException.class);
		}
	}

	@Nested
	@DisplayName("상품 조회")
	class ProductRetrieval {

		@Test
		@DisplayName("존재하지 않는 상품 ID로 재고 변경을 시도하면 예외가 발생한다.")
		void 상품_없음_예외() {
			// given
			Long productId = 999L;
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.INCREASE, 5);

			given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> useCase.updateStock(command))
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