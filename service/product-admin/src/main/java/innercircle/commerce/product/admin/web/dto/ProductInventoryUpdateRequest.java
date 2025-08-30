package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.admin.application.dto.ProductInventoryUpdateCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 상품 재고 조정 요청 DTO
 * 
 * 클라이언트로부터 재고 조정 요청을 받기 위한 데이터 전송 객체입니다.
 *
 * @param operationType 재고 연산 타입 (INCREASE/DECREASE)
 * @param quantity 조정할 재고 수량 (양수)
 * @author 황인웅
 * @version 1.0.0
 */
public record ProductInventoryUpdateRequest(
		@NotNull(message = "작업 유형은 필수입니다.")
		StockOperationType operationType,
		
		@NotNull(message = "수량은 필수입니다.")
		@Positive(message = "수량은 양수여야 합니다.")
		Integer quantity
) {
	/**
	 * 요청 DTO를 애플리케이션 명령 객체로 변환합니다.
	 *
	 * @param productId 재고를 조정할 상품의 ID
	 * @return 변환된 ProductInventoryUpdateCommand 객체
	 */
	public ProductInventoryUpdateCommand toCommand(Long productId) {
		return ProductInventoryUpdateCommand.of(productId, operationType, quantity);
	}
}