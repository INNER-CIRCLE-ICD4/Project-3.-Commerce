package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.admin.application.dto.ProductStockAdjustCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 재고 절대값 조정 요청 DTO
 *
 * @author 황인웅
 * @version 1.0.0
 */
@Getter
@NoArgsConstructor
public class ProductStockAdjustRequest {
	
	@NotNull(message = "설정할 재고 수량은 필수입니다.")
	@PositiveOrZero(message = "설정할 재고 수량은 0 이상이어야 합니다.")
	private Integer quantity;

	public ProductStockAdjustRequest(Integer quantity) {
		this.quantity = quantity;
	}

	/**
	 * 명령 객체로 변환합니다.
	 */
	public ProductStockAdjustCommand toCommand(Long productId) {
		return ProductStockAdjustCommand.of(productId, this.quantity);
	}
}