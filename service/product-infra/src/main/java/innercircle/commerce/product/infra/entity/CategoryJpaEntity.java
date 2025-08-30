package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category JPA 엔티티
 */
@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryJpaEntity {

	@Id
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(name = "parent_id")
	private Long parentId;

	@Column(nullable = false)
	private Integer level;

	@Column(name = "sort_order")
	private Integer sortOrder;

	@Column(nullable = false)
	private Boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
	private List<ProductJpaEntity> products = new ArrayList<>();

	/**
	 * Domain Category 객체에서 JPA Entity로 변환
	 */
	public static CategoryJpaEntity from(Category category) {
		CategoryJpaEntity entity = new CategoryJpaEntity();
		entity.id = category.getId();
		entity.name = category.getName();
		entity.description = category.getDescription();
		entity.parentId = category.getParentId();
		entity.level = category.getLevel();
		entity.sortOrder = category.getSortOrder();
		entity.active = category.getActive();
		entity.createdAt = category.getCreatedAt();
		entity.updatedAt = category.getUpdatedAt();
		return entity;
	}

	/**
	 * Domain Category 객체로 변환
	 */
	public Category toDomain() {
		return Category.restore(
			this.id,
			this.name,
			this.description,
			this.parentId,
			this.level,
			this.sortOrder,
			this.active,
			this.createdAt,
			this.updatedAt
		);
	}
}