package innercircle.commerce.product.core.application.dto;

public record ProductUpdateCommand(
        Long productId,
        String name,
        Integer basePrice,
        String detailContent
) {
}
