package innercircle.commerce.product.admin.application.exception;

public class InvalidCategoryException extends RuntimeException {
	public InvalidCategoryException (Long categoryId) {
		super(categoryId.toString());
	}
}
