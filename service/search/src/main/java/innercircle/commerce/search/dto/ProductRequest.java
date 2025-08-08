package innercircle.commerce.search.dto;

import innercircle.commerce.search.domain.Product;

import java.util.List;

public record ProductRequest(
    String id,
    String name,
    String code,
    String detailContent,
    Integer price,
    List<String> categories,
    String productStatus,
    String saleType) {
    public Product toDocument() {
        return Product.builder()
                .id(id)
                .name(name)
                .code(code)
                .detailContent(detailContent)
                .price(price)
                .categories(categories)
                .productStatus(productStatus)
                .saleType(saleType)
                .build();
    }
}
