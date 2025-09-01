package innercircle.commerce.product.api.web.dto;

/**
 * 재고 조정 연산 타입을 나타내는 열거형
 *
 * @author 황인웅
 * @version 1.0.0
 */
public enum StockOperationType {
	/** 재고 증가 */
	INCREASE,
	/** 재고 감소 */
	DECREASE
}