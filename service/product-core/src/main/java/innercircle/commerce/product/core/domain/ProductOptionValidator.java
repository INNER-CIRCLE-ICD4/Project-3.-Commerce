package innercircle.commerce.product.core.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class ProductOptionValidator {

	protected static void validateProductId(Long productId) {
		if(productId == null) {
			throw new IllegalArgumentException("상품 ID는 필수 입력 값 입니다.");
		}
	}

	protected static void validateName(String name) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("옵션명은 필수입니다");
		}
	}

	protected static void validateSortOrder(int sortOrder) {
		if (sortOrder < 0) {
			throw new IllegalArgumentException("옵션 정렬 순서는 0 이상이어야 합니다");
		}
	}

	protected static void validateItems(List<ProductOptionItem> items) {
		if (CollectionUtils.isEmpty(items)) {
			throw new IllegalArgumentException("옵션 아이템은 최소 1개 이상 필요합니다");
		}
	}
}