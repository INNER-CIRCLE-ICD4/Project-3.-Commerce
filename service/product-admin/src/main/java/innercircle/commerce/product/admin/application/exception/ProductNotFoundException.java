package innercircle.commerce.product.admin.application.exception;

public class ProductNotFoundException extends RuntimeException {
	public ProductNotFoundException (Long productId) {
		super(productId.toString());
	}
}
