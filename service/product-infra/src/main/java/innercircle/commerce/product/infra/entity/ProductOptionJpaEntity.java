package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.ProductOption;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;

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

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "is_required", nullable = false)
	private boolean isRequired;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	/**
	 * Domain ProductOption 객체에서 JPA Entity로 변환
	 */
	public static ProductOptionJpaEntity from(ProductOption productOption) {
		ProductOptionJpaEntity entity = new ProductOptionJpaEntity();
		entity.id = productOption.getId();
		entity.productId = productOption.getProductId();
		entity.name = productOption.getName();
		entity.isRequired = productOption.isRequired();
		entity.sortOrder = productOption.getSortOrder();
		return entity;
	}

	/**
	 * Domain ProductOption 객체로 변환
	 * ProductOptionItem은 별도 조회하여 설정
	 */
	public ProductOption toDomain() {
		return ProductOption.restore(
			this.id,
			this.productId,
			this.name,
			this.isRequired,
			this.sortOrder,
			Collections.emptyList() // ProductOptionItem은 별도 조회
		);
	}
}