package innercircle.commerce.product.admin.application.exception;

public class NotFoundImageException extends RuntimeException {
	public NotFoundImageException (String imagePath) {
		super(imagePath);
	}
}
