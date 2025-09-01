package innercircle.commerce.product.api.application;

import innercircle.commerce.product.api.application.dto.ProductInventoryUpdateCommand;
import innercircle.commerce.product.api.application.exception.StockConflictException;
import innercircle.commerce.product.api.web.dto.StockOperationType;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductInventoryFacadeTest {

	@InjectMocks
	private ProductInventoryFacade facade;

	@Mock
	private ProductInventoryUpdateUseCase useCase;

	@Nested
	@DisplayName("재시도 로직 테스트")
	class RetryLogicTest {

		@Test
		@DisplayName("첫 번째 시도에서 성공하면 재시도하지 않는다.")
		void 첫_시도_성공() throws InterruptedException {
			// given
			Long productId = 1L;
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.INCREASE, 10);
			Product expectedProduct = createTestProduct(productId, 110, 1L);

			given(useCase.updateStock(command)).willReturn(expectedProduct);

			// when
			facade.updateStockWithRetry(command);

			// then
			verify(useCase, times(1)).updateStock(command);
		}

		@Test
		@DisplayName("첫 번째 시도에서 실패하고 두 번째 시도에서 성공하면 2회 시도한다.")
		void 두번째_시도_성공() throws InterruptedException {
			// given
			Long productId = 1L;
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.DECREASE, 5);
			Product expectedProduct = createTestProduct(productId, 95, 2L);

			given(useCase.updateStock(command))
					.willThrow(new StockConflictException("첫 번째 충돌"))
					.willReturn(expectedProduct);

			// when
			facade.updateStockWithRetry(command);

			// then
			verify(useCase, times(2)).updateStock(command);
		}

		@Test
		@DisplayName("세 번째 시도에서 성공하면 3회 시도한다.")
		void 세번째_시도_성공() throws InterruptedException {
			// given
			Long productId = 1L;
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.INCREASE, 15);
			Product expectedProduct = createTestProduct(productId, 115, 3L);

			given(useCase.updateStock(command))
					.willThrow(new StockConflictException("첫 번째 충돌"))
					.willThrow(new StockConflictException("두 번째 충돌"))
					.willReturn(expectedProduct);

			// when
			facade.updateStockWithRetry(command);

			// then
			verify(useCase, times(3)).updateStock(command);
		}

		@Test
		@DisplayName("StockConflictException이 아닌 다른 예외는 재시도하지 않고 바로 던진다.")
		void 다른_예외는_재시도_안함() {
			// given
			Long productId = 1L;
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.INCREASE, 5);

			given(useCase.updateStock(command))
					.willThrow(new RuntimeException("다른 종류의 예외"));

			// when & then
			assertThatThrownBy(() -> facade.updateStockWithRetry(command))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("다른 종류의 예외");

			verify(useCase, times(1)).updateStock(command);
		}
	}

	@Nested
	@DisplayName("동시성 테스트")
	class ConcurrencyTest {

		@Test
		@DisplayName("여러 스레드가 동시에 Facade를 통해 재고 변경 시 모든 요청이 성공할 때까지 재시도한다.")
		void 멀티스레드_재시도_테스트() throws InterruptedException {
			// given
			Long productId = 1L;
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.DECREASE, 1);

			AtomicInteger successCount = new AtomicInteger(0);
			int threadCount = 5;
			CountDownLatch latch = new CountDownLatch(threadCount);

			// Mock 설정 - 일부 충돌 후 성공
			given(useCase.updateStock(any()))
					.willReturn(createTestProduct(productId, 99, 1L)) // 첫 번째 성공
					.willThrow(new StockConflictException("충돌1"))
					.willReturn(createTestProduct(productId, 98, 2L)) // 재시도 후 성공
					.willThrow(new StockConflictException("충돌2"))
					.willReturn(createTestProduct(productId, 97, 3L)) // 재시도 후 성공
					.willThrow(new StockConflictException("충돌3"))
					.willReturn(createTestProduct(productId, 96, 4L)) // 재시도 후 성공
					.willThrow(new StockConflictException("충돌4"))
					.willReturn(createTestProduct(productId, 95, 5L)); // 재시도 후 성공

			// when
			try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
				for (int i = 0; i < threadCount; i++) {
					executor.submit(() -> {
						try {
							facade.updateStockWithRetry(command);
							successCount.incrementAndGet();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						} finally {
							latch.countDown();
						}
					});
				}

				latch.await();
			}

			// then - 무한 재시도로 모든 요청이 성공해야 함
			assertThat(successCount.get()).isEqualTo(threadCount);
		}

		@Test
		@DisplayName("인터럽트 발생 시 InterruptedException이 전파된다.")
		void 인터럽트_예외_처리() throws InterruptedException {
			// given
			Long productId = 1L;
			ProductInventoryUpdateCommand command = ProductInventoryUpdateCommand.of(productId, StockOperationType.INCREASE, 10);

			given(useCase.updateStock(command))
					.willThrow(new StockConflictException("첫 번째 충돌"));

			// when
			Thread currentThread = Thread.currentThread();
			Thread interruptThread = new Thread(() -> {
				try {
					Thread.sleep(150); // Facade가 재시도 대기 중일 때 인터럽트
					currentThread.interrupt();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});

			interruptThread.start();

			// then
			assertThatThrownBy(() -> facade.updateStockWithRetry(command))
					.isInstanceOf(InterruptedException.class);

			interruptThread.join();
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