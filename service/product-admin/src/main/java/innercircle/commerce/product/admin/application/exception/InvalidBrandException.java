package innercircle.commerce.product.admin.application.exception;

public class InvalidBrandException extends RuntimeException {
    public InvalidBrandException(Long brandId) {
        super(brandId.toString());
    }
}
