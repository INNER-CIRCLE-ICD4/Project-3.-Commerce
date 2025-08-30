package innercircle.commerce.product.admin.application.dto;

import innercircle.commerce.product.admin.web.dto.StockOperationType;
import lombok.Builder;
import lombok.Getter;

/**
 * 상품 재고 조정을 위한 명령 객체
 * 
 * 상품의 재고를 증가 또는 감소시키기 위한 필요한 정보를 담고 있습니다.
 *
 * @author 황인웅
 * @version 1.0.0
 */
@Getter
@Builder
public class ProductInventoryUpdateCommand {
	private final Long productId;
	private final StockOperationType operationType;
	private final Integer quantity;

	/**
	 * ProductInventoryUpdateCommand 인스턴스를 생성합니다.
	 *
	 * @param productId 재고를 조정할 상품 ID
	 * @param operationType 재고 연산 타입 (INCREASE/DECREASE)
	 * @param quantity 조정할 재고 수량
	 * @return 생성된 명령 객체
	 */
	public static ProductInventoryUpdateCommand of(Long productId, StockOperationType operationType, Integer quantity) {
		return ProductInventoryUpdateCommand.builder()
				.productId(productId)
				.operationType(operationType)
				.quantity(quantity)
				.build();
	}
}