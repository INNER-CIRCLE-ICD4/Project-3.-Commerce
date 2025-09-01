package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductAdminInfo;
import innercircle.commerce.product.admin.application.dto.ProductStockAdjustCommand;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 재고 절대값 조정을 처리하는 애플리케이션 서비스
 * 
 * 관리자 또는 판매자가 창고 실사 등의 목적으로 
 * 재고 수량을 직접 설정할 때 사용합니다.
 *
 * @author 황인웅
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStockAdjustUseCase {
	private final ProductRepository productRepository;

	/**
	 * 상품의 재고를 절대값으로 조정합니다.
	 * 
	 * 기존 재고 수량과 관계없이 지정된 수량으로 재고를 설정합니다.
	 * 관리 목적의 재고 조정에 사용됩니다.
	 *
	 * @param command 재고 조정 명령 객체 (상품 ID, 설정할 수량 포함)
	 * @return 재고가 조정된 상품 객체
	 * @throws ProductNotFoundException 존재하지 않는 상품 ID인 경우
	 * @throws IllegalArgumentException 수량이 음수인 경우 등 비즈니스 규칙 위반 시
	 */
	@Transactional
	public ProductAdminInfo adjustStock(ProductStockAdjustCommand command) {
		log.info("상품 재고 조정 시작 - ProductId: {}, Quantity: {}", command.getProductId(), command.getQuantity());
		
		Product product = productRepository.findById(command.getProductId())
				.orElseThrow(() -> new ProductNotFoundException(command.getProductId()));

		int previousStock = product.getStock();
		product.adjustStock(command.getQuantity());
		
		Product savedProduct = productRepository.save(product);
		
		log.info("상품 재고 조정 완료 - ProductId: {}, 이전 재고: {}, 조정 후 재고: {}", 
				command.getProductId(), previousStock, savedProduct.getStock());
		
		return ProductAdminInfo.from(savedProduct);
	}
}