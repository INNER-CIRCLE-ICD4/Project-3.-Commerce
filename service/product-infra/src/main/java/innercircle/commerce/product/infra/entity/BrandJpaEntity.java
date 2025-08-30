package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.Brand;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Brand JPA 엔티티
 */
@Entity
@Table(name = "brands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandJpaEntity {

	@Id
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(name = "logo_url", length = 255)
	private String logoUrl;

	@Column(name = "website_url", length = 255)
	private String websiteUrl;

	@Column(nullable = false)
	private Boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
	private List<ProductJpaEntity> products = new ArrayList<>();

	/**
	 * Domain Brand 객체에서 JPA Entity로 변환
	 */
	public static BrandJpaEntity from(Brand brand) {
		BrandJpaEntity entity = new BrandJpaEntity();
		entity.id = brand.getId();
		entity.name = brand.getName();
		entity.description = brand.getDescription();
		entity.logoUrl = brand.getLogoUrl();
		entity.websiteUrl = brand.getWebsiteUrl();
		entity.active = brand.getActive();
		entity.createdAt = brand.getCreatedAt();
		entity.updatedAt = brand.getUpdatedAt();
		return entity;
	}

	/**
	 * Domain Brand 객체로 변환
	 */
	public Brand toDomain() {
		return Brand.restore(
			this.id,
			this.name,
			this.description,
			this.logoUrl,
			this.websiteUrl,
			this.active,
			this.createdAt,
			this.updatedAt
		);
	}
}