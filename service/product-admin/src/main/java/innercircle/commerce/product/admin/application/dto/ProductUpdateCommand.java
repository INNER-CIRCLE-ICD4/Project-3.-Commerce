package innercircle.commerce.product.admin.application.dto;

public record ProductUpdateCommand(
        Long productId,
        String name,
        Integer basePrice,
        String detailContent
) {
}
