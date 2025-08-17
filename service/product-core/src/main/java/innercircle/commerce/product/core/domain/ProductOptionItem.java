package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static innercircle.commerce.product.core.domain.ProductOptionItemValidator.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductOptionItem {
	private Long id;
	private Long optionId;
	private String name;
	private Integer additionalPrice;
	private int sortOrder;

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
		productOptionItem.setId(id);
		productOptionItem.setOptionId(optionId);
		productOptionItem.setName(name);
		productOptionItem.setAdditionalPrice(additionalPrice);
		productOptionItem.setSortOrder(sortOrder);
		return productOptionItem;
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
