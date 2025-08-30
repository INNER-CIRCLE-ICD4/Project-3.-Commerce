package innercircle.commerce.product.admin.application.dto;

import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자용 상품 목록 정보
 */
@Getter
@Builder
public class ProductListAdminInfo {
    private final Long id;
    private final String name;
    private final Long categoryId;
    private final Long brandId;
    private final Integer price;
    private final ProductStatus status;
    private final SaleType saleType;
    private final Integer stock;
    private final String thumbnailUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static ProductListAdminInfo from(Product product) {
        String thumbnailUrl = (product.getImages() == null || product.getImages().isEmpty()) ? 
                null : product.getImages().get(0).getUrl();

        return ProductListAdminInfo.builder()
                .id(product.getId())
                .name(product.getName())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .price(product.getPrice())
                .status(product.getStatus())
                .saleType(product.getSaleType())
                .stock(product.getStock())
                .thumbnailUrl(thumbnailUrl)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}