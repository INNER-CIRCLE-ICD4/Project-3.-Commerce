package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.admin.application.dto.ProductListAdminInfo;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 상품 목록 응답 DTO
 */
@Getter
@Builder
public class ProductListResponse {
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

    public static ProductListResponse from(ProductListAdminInfo info) {
        return ProductListResponse.builder()
                .id(info.getId())
                .name(info.getName())
                .categoryId(info.getCategoryId())
                .brandId(info.getBrandId())
                .price(info.getPrice())
                .status(info.getStatus())
                .saleType(info.getSaleType())
                .stock(info.getStock())
                .thumbnailUrl(info.getThumbnailUrl())
                .createdAt(info.getCreatedAt())
                .updatedAt(info.getUpdatedAt())
                .build();
    }
}