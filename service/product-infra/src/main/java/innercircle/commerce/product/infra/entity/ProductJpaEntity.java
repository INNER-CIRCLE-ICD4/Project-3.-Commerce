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
		return entity;
	}

	/**
	 * Domain Product 객체로 변환
	 */
	public Product toDomain () {
		return Product.restore(
			this.id,
			this.name,
			this.categoryId,
			this.brandId,
			this.price,
			this.stock,
			Collections.emptyList(), // ProductOption은 별도 조회
			Collections.emptyList(), // ProductImage는 별도 조회
			this.detailContent,
			this.saleType,
			this.status
		);
	}
}