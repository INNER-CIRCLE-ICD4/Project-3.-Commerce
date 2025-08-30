package innercircle.commerce.product.admin.web.dto;

import innercircle.commerce.product.admin.application.dto.ProductAdminInfo;
import innercircle.commerce.product.admin.application.dto.ProductImageInfo;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 상세 응답 DTO
 */
@Getter
@Builder
public class ProductDetailResponse {
    private final Long id;
    private final String name;
    private final Long categoryId;
    private final Long brandId;
    private final Integer price;
    private final ProductStatus status;
    private final SaleType saleType;
    private final List<ProductImageInfo> images;
    private final String detailContent;
    private final Integer stock;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static ProductDetailResponse from(ProductAdminInfo info) {
        return ProductDetailResponse.builder()
                .id(info.getId())
                .name(info.getName())
                .categoryId(info.getCategoryId())
                .brandId(info.getBrandId())
                .price(info.getPrice())
                .status(info.getStatus())
                .saleType(info.getSaleType())
                .images(info.getImages())
                .detailContent(info.getDetailContent())
                .stock(info.getStock())
                .createdAt(info.getCreatedAt())
                .updatedAt(info.getUpdatedAt())
                .build();
    }
}