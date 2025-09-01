package innercircle.commerce.product.admin.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 상품 재고 절대값 조정을 위한 명령 객체
 * 
 * 관리자나 판매자가 재고 수량을 직접 설정할 때 사용합니다.
 *
 * @author 황인웅
 * @version 1.0.0
 */
@Getter
@Builder
public class ProductStockAdjustCommand {
	private final Long productId;
	private final Integer quantity;

	/**
	 * ProductStockAdjustCommand 인스턴스를 생성합니다.
	 *
	 * @param productId 재고를 조정할 상품 ID
	 * @param quantity 설정할 재고 수량 (절대값)
	 * @return 생성된 명령 객체
	 */
	public static ProductStockAdjustCommand of(Long productId, Integer quantity) {
		return ProductStockAdjustCommand.builder()
				.productId(productId)
				.quantity(quantity)
				.build();
	}
}