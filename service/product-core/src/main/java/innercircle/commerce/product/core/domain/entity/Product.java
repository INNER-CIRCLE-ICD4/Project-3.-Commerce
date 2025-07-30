package innercircle.commerce.product.core.domain.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 상품 애그리거트 루트
 */
@Getter
public class Product {

	private Long id;
	private String name;
	private String code;
	private Long categoryId;
	private Long brandId;
	private Integer basePrice;
	private ProductStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	/**
	 * 상품 생성자
	 */
	private Product (String name, Long categoryId, Long brandId, Integer basePrice) {
		validateProductCreation(name, categoryId, brandId, basePrice);

		this.name = name;
		this.categoryId = categoryId;
		this.brandId = brandId;
		this.basePrice = basePrice;
		this.status = ProductStatus.ACTIVE; // 기본 상태는 ACTIVE
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 상품 생성 팩토리 메서드
	 *
	 * @param name       상품명 (2-50자)
	 * @param categoryId 카테고리 ID
	 * @param brandId    브랜드 ID
	 * @param basePrice  기본 가격 (0 이상)
	 * @return 생성된 상품
	 */
	public static Product create (String name, Long categoryId, Long brandId, Integer basePrice) {
		return new Product(name, categoryId, brandId, basePrice);
	}

	/**
	 * 상품 정보 업데이트
	 */
	public void update (String name, Long categoryId, Long brandId, Integer basePrice) {
		validateProductCreation(name, categoryId, brandId, basePrice);

		this.name = name;
		this.categoryId = categoryId;
		this.brandId = brandId;
		this.basePrice = basePrice;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 상품 상태 변경
	 */
	public void changeStatus (ProductStatus status) {
		validateStatus(status);
		this.status = status;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 상품 생성/업데이트 시 유효성 검증
	 */
	private void validateProductCreation (String name, Long categoryId, Long brandId, Integer basePrice) {
		validateName(name);
		validateCategoryId(categoryId);
		validateBrandId(brandId);
		validateBasePrice(basePrice);
	}

	/**
	 * 상품명 유효성 검증
	 */
	private void validateName (String name) {
		if (name == null) {
			throw new IllegalArgumentException("상품명은 필수입니다.");
		}

		if (name.trim().isEmpty()) {
			throw new IllegalArgumentException("상품명은 필수입니다.");
		}

		if (name.length() < 2 || name.length() > 50) {
			throw new IllegalArgumentException("상품명은 2자 이상 50자 이하여야 합니다.");
		}
	}

	/**
	 * 카테고리 ID 유효성 검증
	 */
	private void validateCategoryId (Long categoryId) {
		if (categoryId == null) {
			throw new IllegalArgumentException("카테고리 ID는 필수입니다.");
		}
	}

	/**
	 * 브랜드 ID 유효성 검증
	 */
	private void validateBrandId (Long brandId) {
		if (brandId == null) {
			throw new IllegalArgumentException("브랜드 ID는 필수입니다.");
		}
	}

	/**
	 * 기본 가격 유효성 검증
	 */
	private void validateBasePrice (Integer basePrice) {
		if (basePrice == null) {
			throw new IllegalArgumentException("가격은 필수입니다.");
		}

		if (basePrice < 0) {
			throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
		}
	}

	/**
	 * 상품 상태 유효성 검증
	 */
	private void validateStatus (ProductStatus status) {
		if (status == null) {
			throw new IllegalArgumentException("상품 상태는 필수입니다.");
		}
	}
}
