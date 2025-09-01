package innercircle.commerce.product.api.application.exception;

/**
 * 재고 조정 중 동시성 충돌이 발생했을 때 던지는 예외
 * 
 * 여러 사용자가 동시에 같은 상품의 재고를 수정하려 할 때 
 * 낙관적 락킹에 의해 발생하는 충돌을 나타냅니다.
 *
 * @author 황인웅
 * @version 1.0.0
 */
public class StockConflictException extends RuntimeException {
	
	/**
	 * 메시지와 함께 StockConflictException을 생성합니다.
	 *
	 * @param message 예외 메시지
	 */
	public StockConflictException(String message) {
		super(message);
	}

	/**
	 * 메시지와 원인 예외와 함께 StockConflictException을 생성합니다.
	 *
	 * @param message 예외 메시지
	 * @param cause 원인이 된 예외
	 */
	public StockConflictException(String message, Throwable cause) {
		super(message, cause);
	}
}