package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.ProductImage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ProductImage JPA 엔티티
 */
@Entity
@Table(name = "product_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImageJpaEntity {
	@Id
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private ProductJpaEntity product;

	@Column(nullable = false, length = 2000)
	private String url;

	@Column(name = "original_name", length = 255)
	private String originalName;


	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	/**
	 * Domain ProductImage 객체에서 JPA Entity로 변환
	 */
	public static ProductImageJpaEntity from(ProductImage productImage) {
		ProductImageJpaEntity entity = new ProductImageJpaEntity();
		entity.id = productImage.getId();
		// product는 부모 엔티티에서 설정됨
		entity.url = productImage.getUrl();
		entity.originalName = productImage.getOriginalName();
		entity.sortOrder = productImage.getSortOrder();
		return entity;
	}

	/**
	 * 연관관계 편의 메서드
	 */
	public void setProduct(ProductJpaEntity product) {
		this.product = product;
	}

	/**
	 * Domain ProductImage 객체로 변환
	 */
	public ProductImage toDomain() {
		return ProductImage.restore(
			this.id,
			this.product != null ? this.product.getId() : null,
			this.url,
			this.originalName,
			this.sortOrder
		);
	}
}