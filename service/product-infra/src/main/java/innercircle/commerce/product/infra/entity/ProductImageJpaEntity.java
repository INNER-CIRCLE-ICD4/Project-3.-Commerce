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

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(nullable = false, length = 2000)
	private String url;

	@Column(name = "original_name", length = 255)
	private String originalName;

	@Column(name = "is_main", nullable = false)
	private boolean isMain;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	/**
	 * Domain ProductImage 객체에서 JPA Entity로 변환
	 */
	public static ProductImageJpaEntity from(ProductImage productImage) {
		ProductImageJpaEntity entity = new ProductImageJpaEntity();
		entity.id = productImage.getId();
		entity.productId = productImage.getProductId();
		entity.url = productImage.getUrl();
		entity.originalName = productImage.getOriginalName();
		entity.isMain = productImage.isMain();
		entity.sortOrder = productImage.getSortOrder();
		return entity;
	}

	/**
	 * Domain ProductImage 객체로 변환
	 */
	public ProductImage toDomain() {
		return ProductImage.restore(
			this.id,
			this.productId,
			this.url,
			this.originalName,
			this.isMain,
			this.sortOrder
		);
	}
}