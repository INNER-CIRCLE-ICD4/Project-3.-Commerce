package innercircle.commerce.product.core.application.exception;

public class DuplicateProductNameException extends RuntimeException {
	public DuplicateProductNameException (String productName) {
		super(productName);
	}
}
