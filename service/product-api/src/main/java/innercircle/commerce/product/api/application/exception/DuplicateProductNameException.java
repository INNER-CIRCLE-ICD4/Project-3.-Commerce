package innercircle.commerce.product.api.application.exception;

public class DuplicateProductNameException extends RuntimeException {
	public DuplicateProductNameException (String productName) {
		super(productName);
	}
}
