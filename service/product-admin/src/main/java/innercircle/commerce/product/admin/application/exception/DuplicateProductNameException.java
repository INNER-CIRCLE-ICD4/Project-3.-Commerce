package innercircle.commerce.product.admin.application.exception;

public class DuplicateProductNameException extends RuntimeException {
	public DuplicateProductNameException (String productName) {
		super(productName);
	}
}
