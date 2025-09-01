package innercircle.commerce.product.admin.application.dto;

import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 상품 상세 정보
 */
@Getter
@Builder
public class ProductAdminInfo {
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

    public static ProductAdminInfo from(Product product) {
        return ProductAdminInfo.builder()
                .id(product.getId())
                .name(product.getName())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .price(product.getPrice())
                .status(product.getStatus())
                .saleType(product.getSaleType())
                .images(product.getImages() == null ? List.of() : 
                        product.getImages().stream()
                                .map(ProductImageInfo::from)
                                .toList())
                .detailContent(product.getDetailContent())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}