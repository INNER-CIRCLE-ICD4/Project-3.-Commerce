package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Product JPA 엔티티
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductJpaEntity {
	@Id
	private Long id;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(name = "category_id", nullable = false)
	private Long categoryId;

	@Column(name = "brand_id", nullable = false)
	private Long brandId;

	@Column(nullable = false)
	private Integer price;

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

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private List<ProductImageJpaEntity> images = new ArrayList<>();

	/**
	 * Domain Product 객체에서 JPA Entity로 변환
	 */
	public static ProductJpaEntity from (Product product) {
		ProductJpaEntity entity = new ProductJpaEntity();
		entity.id = product.getId();
		entity.name = product.getName();
		entity.categoryId = product.getCategoryId();
		entity.brandId = product.getBrandId();
		entity.price = product.getPrice();
		entity.status = product.getStatus();
		entity.saleType = product.getSaleType();
		entity.detailContent = product.getDetailContent();
		entity.stock = product.getStock();
		entity.createdAt = product.getCreatedAt();
		entity.updatedAt = product.getUpdatedAt();
		
		// 이미지 변환
		if (product.getImages() != null) {
			List<ProductImageJpaEntity> imageEntities = product.getImages().stream()
					.map(ProductImageJpaEntity::from)
					.toList();
			entity.images.clear();
			entity.images.addAll(imageEntities);
		}
		
		return entity;
	}

	/**
	 * Domain Product 객체로 변환
	 */
	public Product toDomain () {
		List<innercircle.commerce.product.core.domain.ProductImage> domainImages = 
			this.images != null ? 
				this.images.stream().map(ProductImageJpaEntity::toDomain).toList() : 
				Collections.emptyList();
		
		return Product.restore(
			this.id,
			this.name,
			this.categoryId,
			this.brandId,
			this.price,
			this.stock,
			Collections.emptyList(), // ProductOption은 별도 조회
			domainImages, // ProductImage 포함
			this.detailContent,
			this.saleType,
			this.status
		);
	}
}