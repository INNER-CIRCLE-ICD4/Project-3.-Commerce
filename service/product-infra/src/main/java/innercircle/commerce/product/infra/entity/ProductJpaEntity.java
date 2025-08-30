package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private CategoryJpaEntity category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "brand_id", nullable = false)
	private BrandJpaEntity brand;

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

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<ProductImageJpaEntity> images = new ArrayList<>();

	@Size(max = 10)
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<ProductOptionJpaEntity> options = new ArrayList<>();

	/**
	 * Domain Product 객체에서 JPA Entity로 변환 (연관 엔티티는 외부에서 설정 필요)
	 */
	public static ProductJpaEntity from(Product product, CategoryJpaEntity category, BrandJpaEntity brand) {
		ProductJpaEntity entity = new ProductJpaEntity();
		entity.id = product.getId();
		entity.name = product.getName();
		entity.category = category;
		entity.brand = brand;
		entity.price = product.getPrice();
		entity.status = product.getStatus();
		entity.saleType = product.getSaleType();
		entity.detailContent = product.getDetailContent();
		entity.stock = product.getStock();
		entity.createdAt = product.getCreatedAt();
		entity.updatedAt = product.getUpdatedAt();

		if (product.getImages() != null) {
			List<ProductImageJpaEntity> imageEntities = product.getImages().stream()
					.map(domainImage -> {
						ProductImageJpaEntity imageEntity = ProductImageJpaEntity.from(domainImage);
						imageEntity.setProduct(entity);
						return imageEntity;
					})
					.collect(Collectors.toList());
			entity.images.clear();
			entity.images.addAll(imageEntities);
		}

		if (product.getOptions() != null) {
			List<ProductOptionJpaEntity> optionEntities = product.getOptions().stream()
					.map(domainOption -> {
						ProductOptionJpaEntity optionEntity = ProductOptionJpaEntity.from(domainOption);
						optionEntity.setProduct(entity);
						return optionEntity;
					})
					.collect(Collectors.toList());
			entity.options.clear();
			entity.options.addAll(optionEntities);
		}
		return entity;
	}

	/**
	 * Domain Product 객체로 변환
	 */
	public Product toDomain() {
		List<innercircle.commerce.product.core.domain.ProductImage> domainImages =
				this.images != null ?
						this.images.stream().map(ProductImageJpaEntity::toDomain).collect(Collectors.toList()) :
						Collections.emptyList();

		List<innercircle.commerce.product.core.domain.ProductOption> domainOptions =
				this.options != null ?
						this.options.stream().map(ProductOptionJpaEntity::toDomain).collect(Collectors.toList()) :
						Collections.emptyList();

		return Product.restore(
				this.id,
				this.name,
				this.category != null ? this.category.getId() : null,
				this.brand != null ? this.brand.getId() : null,
				this.price,
				this.stock,
				domainOptions,
				domainImages,
				this.detailContent,
				this.saleType,
				this.status
		);
	}
}
