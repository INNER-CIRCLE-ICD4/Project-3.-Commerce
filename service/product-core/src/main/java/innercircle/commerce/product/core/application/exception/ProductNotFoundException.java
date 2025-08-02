package innercircle.commerce.product.core.application.exception;

public class ProductNotFoundException extends RuntimeException {
	public ProductNotFoundException (Long productId) {
		super(productId.toString());
	}
}
