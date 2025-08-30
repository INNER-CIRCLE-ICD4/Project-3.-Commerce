package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static innercircle.commerce.product.core.domain.ProductOptionItemValidator.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductOptionItem {
	private Long id;
	private Long optionId;
	private String name;
	private Integer additionalPrice;
	private int sortOrder;
	private ProductStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	/**
	 * 상품 옵션 아이템을 생성합니다.
	 * ID가 제공되지 않으면 Snowflake를 통해 새로운 ID를 생성합니다.
	 *
	 * @param optionId        옵션 ID
	 * @param name            아이템명
	 * @param additionalPrice 추가 가격
	 * @param sortOrder       정렬 순서
	 * @return 생성된 상품 옵션 아이템 객체
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static ProductOptionItem create(
			Long optionId, String name, Integer additionalPrice, int sortOrder
	) {
		return create(null, optionId, name, additionalPrice, sortOrder);
	}

	/**
	 * 상품 옵션 아이템을 생성합니다.
	 * ID가 제공되면 해당 ID를 사용하고, 없으면 Snowflake를 통해 새로운 ID를 생성합니다.
	 *
	 * @param id              아이템 ID (nullable)
	 * @param optionId        옵션 ID
	 * @param name            아이템명
	 * @param additionalPrice 추가 가격
	 * @param sortOrder       정렬 순서
	 * @return 생성된 상품 옵션 아이템 객체
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static ProductOptionItem create(
			Long id, Long optionId, String name, Integer additionalPrice, int sortOrder
	) {
		ProductOptionItem productOptionItem = new ProductOptionItem();
		LocalDateTime now = LocalDateTime.now();
		productOptionItem.setId(id);
		productOptionItem.setOptionId(optionId);
		productOptionItem.setName(name);
		productOptionItem.setAdditionalPrice(additionalPrice);
		productOptionItem.setSortOrder(sortOrder);
		productOptionItem.status = ProductStatus.SALE;
		productOptionItem.createdAt = now;
		productOptionItem.updatedAt = now;
		return productOptionItem;
	}

	/**
	 * 기존 상품 옵션 아이템을 복원합니다.
	 * 주로 데이터베이스에서 조회한 데이터로 객체를 복원할 때 사용됩니다.
	 *
	 * @param id              아이템 ID
	 * @param optionId        옵션 ID
	 * @param name            아이템명
	 * @param additionalPrice 추가 가격
	 * @param sortOrder       정렬 순서
	 * @param status          상태
	 * @param createdAt       생성일시
	 * @param updatedAt       수정일시
	 * @return 복원된 상품 옵션 아이템 객체
	 */
	public static ProductOptionItem restore(
			Long id, Long optionId, String name, Integer additionalPrice, int sortOrder,
			ProductStatus status, LocalDateTime createdAt, LocalDateTime updatedAt
	) {
		ProductOptionItem productOptionItem = new ProductOptionItem();
		productOptionItem.id = id;
		productOptionItem.optionId = optionId;
		productOptionItem.name = name;
		productOptionItem.additionalPrice = additionalPrice;
		productOptionItem.sortOrder = sortOrder;
		productOptionItem.status = status;
		productOptionItem.createdAt = createdAt;
		productOptionItem.updatedAt = updatedAt;
		return productOptionItem;
	}

	/**
	 * 기존 상품 옵션 아이템을 복원합니다. (하위 호환성을 위한 오버로드 메서드)
	 * 
	 * @param id              아이템 ID
	 * @param optionId        옵션 ID
	 * @param name            아이템명
	 * @param additionalPrice 추가 가격
	 * @param sortOrder       정렬 순서
	 * @return 복원된 상품 옵션 아이템 객체
	 */
	public static ProductOptionItem restore(
			Long id, Long optionId, String name, Integer additionalPrice, int sortOrder
	) {
		LocalDateTime now = LocalDateTime.now();
		return restore(id, optionId, name, additionalPrice, sortOrder, ProductStatus.SALE, now, now);
	}

	/**
	 * 상품 옵션 아이템을 논리 삭제합니다.
	 */
	public void delete() {
		if (this.status == ProductStatus.DELETE) {
			throw new IllegalArgumentException("이미 삭제된 옵션 아이템입니다.");
		}
		
		this.status = ProductStatus.DELETE;
		this.updatedAt = LocalDateTime.now();
	}

	private void setId(Long id) {
		this.id = id != null ? id : IdGenerator.generateId();
	}

	private void setOptionId(Long optionId) {
		if (optionId == null) {
			throw new IllegalArgumentException("옵션 ID는 필수입니다");
		}
		this.optionId = optionId;
	}

	private void setName(String name) {
		validateName(name);
		this.name = name;
	}

	private void setAdditionalPrice(Integer additionalPrice) {
		validateAdditionalPrice(additionalPrice);
		this.additionalPrice = additionalPrice;
	}

	private void setSortOrder(int sortOrder) {
		validateSortOrder(sortOrder);
		this.sortOrder = sortOrder;
	}
}
