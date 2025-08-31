package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductInventoryUpdateCommand;
import innercircle.commerce.product.admin.application.exception.StockConflictException;
import innercircle.commerce.product.core.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 상품 재고 관리에 대한 Facade 패턴 구현체
 * <p>
 * 낙관적 락 충돌 발생 시 자동으로 재시도를 수행하여
 * 동시성 문제를 해결합니다.
 *
 * @author 황인웅
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductInventoryFacade {
	private static final long RETRY_DELAY_MS = 100;

	private final ProductInventoryUpdateUseCase productInventoryUpdateUseCase;

	/**
	 * 재시도 로직을 포함한 상품 재고 업데이트
	 * <p>
	 * StockConflictException 발생 시 재시도를 통해 재고 업데이트를 수행,
	 * 각 재시도 사이에는 100ms의 지연시간을 둡니다.
	 *
	 * @param command 재고 조정 명령 객체
	 * @return 재고가 조정된 상품 객체
	 * @throws StockConflictException 최대 재시도 횟수 초과 시
	 * @throws RuntimeException       기타 예상치 못한 예외 발생 시
	 */
	public void updateStockWithRetry (ProductInventoryUpdateCommand command) throws InterruptedException {
		while (true) {
			try {
				productInventoryUpdateUseCase.updateStock(command);
				break;
			} catch (StockConflictException e) {
				log.error("{} 발생, 업데이트 실패", e.getClass().getSimpleName(), e);
				Thread.sleep(RETRY_DELAY_MS); // 점진적 지연
			}
		}
	}
}