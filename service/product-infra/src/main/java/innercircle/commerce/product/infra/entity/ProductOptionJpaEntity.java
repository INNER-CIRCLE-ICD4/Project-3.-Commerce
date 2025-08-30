package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.ProductOption;
import innercircle.commerce.product.core.domain.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductOption JPA 엔티티
 */
@Entity
@Table(name = "product_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOptionJpaEntity {
	@Id
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private ProductJpaEntity product;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "is_required", nullable = false)
	private boolean isRequired;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProductStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Size(min = 1)
	@OneToMany(mappedBy = "productOption", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<ProductOptionItemJpaEntity> items = new ArrayList<>();

	/**
	 * 연관관계 편의 메서드
	 */
	protected void setProduct(ProductJpaEntity product) {
		this.product = product;
	}

	/**
	 * Domain ProductOption 객체에서 JPA Entity로 변환
	 */
	public static ProductOptionJpaEntity from(ProductOption productOption) {
		ProductOptionJpaEntity entity = new ProductOptionJpaEntity();
		entity.id = productOption.getId();
		entity.name = productOption.getName();
		entity.isRequired = productOption.isRequired();
		entity.sortOrder = productOption.getSortOrder();
		entity.status = productOption.getStatus();
		entity.createdAt = productOption.getCreatedAt();
		entity.updatedAt = productOption.getUpdatedAt();

		if (productOption.getItems() != null) {
			List<ProductOptionItemJpaEntity> itemEntities = productOption.getItems().stream()
					.map(domainItem -> {
						ProductOptionItemJpaEntity itemEntity = ProductOptionItemJpaEntity.from(domainItem);
						itemEntity.setProductOption(entity);
						return itemEntity;
					})
					.collect(Collectors.toList());
			entity.items.clear();
			entity.items.addAll(itemEntities);
		}
		return entity;
	}

	/**
	 * Domain ProductOption 객체로 변환
	 */
	public ProductOption toDomain() {
		List<innercircle.commerce.product.core.domain.ProductOptionItem> domainItems =
				this.items != null ?
						this.items.stream().map(ProductOptionItemJpaEntity::toDomain).collect(Collectors.toList()) :
						Collections.emptyList();

		return ProductOption.restore(
			this.id,
			this.product != null ? this.product.getId() : null,
			this.name,
			this.isRequired,
			this.sortOrder,
			domainItems,
			this.status,
			this.createdAt,
			this.updatedAt
		);
	}
}
