package innercircle.commerce.product.api.application.exception;

public class InvalidBrandException extends RuntimeException {
    public InvalidBrandException(Long brandId) {
        super(brandId.toString());
    }
}
