package innercircle.commerce.product.api.web.dto;

import innercircle.commerce.product.api.application.dto.ProductInventoryUpdateCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 재고 증감 요청 DTO
 *
 * @author 황인웅
 * @version 1.0.0
 */
@Getter
@NoArgsConstructor
public class ProductStockUpdateRequest {
	
	@NotNull(message = "조정할 재고 수량은 필수입니다.")
	@Positive(message = "조정할 재고 수량은 양수여야 합니다.")
	private Integer quantity;

	public ProductStockUpdateRequest(Integer quantity) {
		this.quantity = quantity;
	}

	/**
	 * 재고 증가 명령 객체로 변환합니다.
	 */
	public ProductInventoryUpdateCommand toIncreaseCommand(Long productId) {
		return ProductInventoryUpdateCommand.of(productId, StockOperationType.INCREASE, this.quantity);
	}

	/**
	 * 재고 감소 명령 객체로 변환합니다.
	 */
	public ProductInventoryUpdateCommand toDecreaseCommand(Long productId) {
		return ProductInventoryUpdateCommand.of(productId, StockOperationType.DECREASE, this.quantity);
	}
}