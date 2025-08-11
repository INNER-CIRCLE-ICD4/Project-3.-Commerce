package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.entity.Product;
import innercircle.commerce.product.core.domain.entity.ProductStatus;
import innercircle.commerce.product.core.domain.entity.SaleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Product JPA 엔티티
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, length = 100)
    private String code;

    @Column(name = "leaf_category_id", nullable = false)
    private Long leafCategoryId;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "base_price", nullable = false)
    private Integer basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", nullable = false)
    private SaleType saleType;

    @Column(name = "detail_content", columnDefinition = "TEXT")
    private String detailContent;

    @Column(nullable = false)
    private Integer stock;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Domain Product 객체로 변환
     */
    public Product toDomain() {
        return Product.builder()
                .id(this.id)
                .name(this.name)
                .code(this.code)
                .leafCategoryId(this.leafCategoryId)
                .brandId(this.brandId)
                .basePrice(this.basePrice)
                .status(this.status)
                .saleType(this.saleType)
                .options(Collections.emptyList()) // TODO: ProductOption 구현 후 추가
                .images(Collections.emptyList())  // TODO: ProductImage 구현 후 추가
                .detailContent(this.detailContent)
                .stock(this.stock)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * Domain Product 객체에서 JPA Entity로 변환
     */
    public static ProductJpaEntity from(Product product) {
        return ProductJpaEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .code(product.getCode())
                .leafCategoryId(product.getLeafCategoryId())
                .brandId(product.getBrandId())
                .basePrice(product.getBasePrice())
                .status(product.getStatus())
                .saleType(product.getSaleType())
                .detailContent(product.getDetailContent())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}