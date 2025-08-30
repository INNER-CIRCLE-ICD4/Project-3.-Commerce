package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.core.domain.Product;
import lombok.Builder;
import lombok.Getter;

/**
 * 상품 재고 조정 응답 DTO
 * 
 * 재고 조정 완료 후 클라이언트에게 반환되는 응답 데이터를 담고 있습니다.
 *
 * @author 황인웅
 * @version 1.0.0
 */
@Getter
@Builder
public class ProductInventoryUpdateResponse {
	private final Long productId;
	private final String productName;
	private final Integer updatedStock;
	private final Long version;

	/**
	 * Product 도메인 객체로부터 응답 DTO를 생성합니다.
	 *
	 * @param product 재고가 조정된 상품 도메인 객체
	 * @return 생성된 응답 DTO
	 */
	public static ProductInventoryUpdateResponse from(Product product) {
		return ProductInventoryUpdateResponse.builder()
				.productId(product.getId())
				.productName(product.getName())
				.updatedStock(product.getStock())
				.version(product.getVersion())
				.build();
	}
}