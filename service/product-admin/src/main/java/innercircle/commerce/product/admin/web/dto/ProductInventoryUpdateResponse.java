package innercircle.commerce.product.admin.web.dto;

/**
 * 상품 재고 조정 응답 DTO
 * <p>
 * 재고 조정 완료 후 클라이언트에게 반환되는 응답 데이터를 담고 있습니다.
 *
 * @author 황인웅
 * @version 1.0.0
 */
public record ProductInventoryUpdateResponse(String mesaage) {
	/**
	 * 재고 조정 성공 응답을 생성합니다.
	 *
	 * @return 성공 응답 DTO
	 */
	public static ProductInventoryUpdateResponse success () {
		return new ProductInventoryUpdateResponse("상품 재고 변경 성공");
	}
}