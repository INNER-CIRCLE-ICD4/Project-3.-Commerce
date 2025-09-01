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

	@Version
	@Column(nullable = false)
	private Long version;

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
		entity.version = product.getVersion();
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
	 * 기존 JPA Entity를 Domain Product 객체 정보로 업데이트합니다.
	 * 
	 * JPA의 더티 체킹(dirty checking)을 통해 변경사항이 감지되도록
	 * 기존 엔티티의 필드값들을 새로운 값으로 업데이트합니다.
	 * version 필드는 JPA가 자동으로 관리하므로 직접 수정하지 않습니다.
	 *
	 * @param product 업데이트할 도메인 객체
	 * @param category 새로운 카테고리 엔티티
	 * @param brand 새로운 브랜드 엔티티
	 */
	public void updateFrom(Product product, CategoryJpaEntity category, BrandJpaEntity brand) {
		this.name = product.getName();
		this.category = category;
		this.brand = brand;
		this.price = product.getPrice();
		this.status = product.getStatus();
		this.saleType = product.getSaleType();
		this.detailContent = product.getDetailContent();
		this.stock = product.getStock();
		this.updatedAt = product.getUpdatedAt();
		// version은 JPA가 자동으로 관리하므로 업데이트하지 않음

		// 이미지 업데이트
		if (product.getImages() != null) {
			List<ProductImageJpaEntity> imageEntities = product.getImages().stream()
					.map(domainImage -> {
						ProductImageJpaEntity imageEntity = ProductImageJpaEntity.from(domainImage);
						imageEntity.setProduct(this);
						return imageEntity;
					})
					.collect(Collectors.toList());
			this.images.clear();
			this.images.addAll(imageEntities);
		}

		// 옵션 업데이트
		if (product.getOptions() != null) {
			List<ProductOptionJpaEntity> optionEntities = product.getOptions().stream()
					.map(domainOption -> {
						ProductOptionJpaEntity optionEntity = ProductOptionJpaEntity.from(domainOption);
						optionEntity.setProduct(this);
						return optionEntity;
					})
					.collect(Collectors.toList());
			this.options.clear();
			this.options.addAll(optionEntities);
		}
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
				this.version,
				domainOptions,
				domainImages,
				this.detailContent,
				this.saleType,
				this.status,
				this.createdAt,
				this.updatedAt
		);
	}
}
