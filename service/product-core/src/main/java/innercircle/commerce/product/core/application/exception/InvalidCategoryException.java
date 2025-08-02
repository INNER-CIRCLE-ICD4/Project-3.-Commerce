package innercircle.commerce.product.core.application.exception;

public class InvalidCategoryException extends RuntimeException {
	public InvalidCategoryException (Long categoryId) {
		super(categoryId.toString());
	}
}
