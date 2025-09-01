package innercircle.commerce.product.api.application.exception;

public class ProductNotFoundException extends RuntimeException {
	public ProductNotFoundException (Long productId) {
		super(productId.toString());
	}
}