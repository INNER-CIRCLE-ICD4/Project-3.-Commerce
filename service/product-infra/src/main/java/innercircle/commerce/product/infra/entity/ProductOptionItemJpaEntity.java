package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.ProductOptionItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ProductOptionItem JPA 엔티티
 */
@Entity
@Table(name = "product_option_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOptionItemJpaEntity {
	@Id
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "option_id", nullable = false)
	private ProductOptionJpaEntity productOption;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "additional_price", nullable = false)
	private Integer additionalPrice;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	/**
	 * Domain ProductOptionItem 객체에서 JPA Entity로 변환
	 */
	public static ProductOptionItemJpaEntity from(ProductOptionItem productOptionItem) {
		ProductOptionItemJpaEntity entity = new ProductOptionItemJpaEntity();
		entity.id = productOptionItem.getId();
		entity.name = productOptionItem.getName();
		entity.additionalPrice = productOptionItem.getAdditionalPrice();
		entity.sortOrder = productOptionItem.getSortOrder();
		// productOption은 부모 엔티티에서 설정됨
		return entity;
	}

	/**
	 * 연관관계 편의 메서드
	 */
	protected void setProductOption(ProductOptionJpaEntity productOption) {
		this.productOption = productOption;
	}

	/**
	 * Domain ProductOptionItem 객체로 변환
	 */
	public ProductOptionItem toDomain() {
		return ProductOptionItem.restore(
			this.id,
			this.productOption != null ? this.productOption.getId() : null,
			this.name,
			this.additionalPrice,
			this.sortOrder
		);
	}
}