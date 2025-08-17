package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Product JPA 엔티티
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

	public static ProductJpaEntity from (Product product) {
		return null;
	}

	/**
	 * Domain Product 객체로 변환
	 */
	public Product toDomain () {
		return Product.restore(
			this.id,
			this.name,
			this.leafCategoryId,
			this.brandId,
			this.basePrice,
			this.stock,
			Collections.emptyList(), // TODO: ProductOption 구현 후 추가
			Collections.emptyList(), // TODO: ProductImage 구현 후 추가
			this.detailContent,
			this.saleType,
			this.status
		);
	}

	//	/**
	//	 * Domain Product 객체에서 JPA Entity로 변환
	//	 */
	//	public static ProductJpaEntity from (Product product) {
	//		return ProductJpaEntity.builder()
	//							   .id(product.getId())
	//							   .name(product.getName())
	//							   .code(product.getCode())
	//							   .leafCategoryId(product.getLeafCategoryId())
	//							   .brandId(product.getBrandId())
	//							   .basePrice(product.getBasePrice())
	//							   .status(product.getStatus())
	//							   .saleType(product.getSaleType())
	//							   .detailContent(product.getDetailContent())
	//							   .stock(product.getStock())
	//							   .createdAt(product.getCreatedAt())
	//							   .updatedAt(product.getUpdatedAt())
	//							   .build();
	//	}
}