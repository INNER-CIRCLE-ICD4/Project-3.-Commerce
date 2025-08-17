package innercircle.commerce.product.core.domain;

import org.apache.commons.lang3.StringUtils;

public class ProductOptionItemValidator {

	private ProductOptionItemValidator () {
	}

	protected static void validateName (String name) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("옵션 아이템명은 필수입니다");
		}
	}

	protected static void validateAdditionalPrice (Integer additionalPrice) {
		if (additionalPrice == null) {
			throw new IllegalArgumentException("추가 가격은 필수입니다");
		}
		if (additionalPrice < 0) {
			throw new IllegalArgumentException("추가 가격은 0 이상이어야 합니다");
		}
	}

	protected static void validateSortOrder (int sortOrder) {
		if (sortOrder < 0) {
			throw new IllegalArgumentException("옵션 아이템 정렬 순서는 0 이상이어야 합니다");
		}
	}
}