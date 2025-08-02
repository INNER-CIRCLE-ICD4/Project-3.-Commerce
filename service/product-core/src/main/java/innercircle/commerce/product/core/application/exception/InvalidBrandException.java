package innercircle.commerce.product.core.application.exception;

public class InvalidBrandException extends RuntimeException {
    public InvalidBrandException(Long brandId) {
        super(brandId.toString());
    }
}
