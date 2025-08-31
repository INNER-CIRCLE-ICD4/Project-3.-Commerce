package innercircle.commerce.product.admin.application.exception;

public class NotFoundTempImageException extends RuntimeException {
	public NotFoundTempImageException (String tempId) {
		super(tempId);
	}
}
