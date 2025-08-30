package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductInventoryUpdateCommand;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.admin.application.exception.StockConflictException;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 재고 조정을 처리하는 애플리케이션 서비스
 * 
 * 동시성 제어를 위해 낙관적 락킹을 사용하며,
 * 재고 증가/감소 연산을 트랜잭션 내에서 안전하게 처리합니다.
 *
 * @author 황인웅
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductInventoryUpdateUseCase {
	private final ProductRepository productRepository;

	/**
	 * 상품의 재고를 증가 또는 감소시킵니다.
	 * 
	 * 낙관적 락킹을 통해 동시성을 제어하며, 충돌 발생 시 
	 * StockConflictException을 발생시킵니다.
	 *
	 * @param command 재고 조정 명령 객체 (상품 ID, 연산 타입, 수량 포함)
	 * @return 재고가 조정된 상품 객체
	 * @throws ProductNotFoundException 존재하지 않는 상품 ID인 경우
	 * @throws StockConflictException 동시성 충돌로 재고 조정에 실패한 경우
	 * @throws IllegalArgumentException 재고 부족 등 비즈니스 규칙 위반 시
	 */
	@Transactional
	public Product updateStock(ProductInventoryUpdateCommand command) {
		try {
			Product product = productRepository.findById(command.getProductId())
					.orElseThrow(() -> new ProductNotFoundException(command.getProductId()));

			switch (command.getOperationType()) {
				case INCREASE -> product.increaseStock(command.getQuantity());
				case DECREASE -> product.decreaseStock(command.getQuantity());
			}

			return productRepository.save(product);
		} catch (OptimisticLockException e) {
			log.warn("재고 변경 중 동시성 충돌 발생. ProductId: {}, Quantity: {}", 
					command.getProductId(), command.getQuantity());
			throw new StockConflictException("재고 변경 중 충돌이 발생했습니다. 잠시 후 다시 시도해주세요.", e);
		}
	}
}