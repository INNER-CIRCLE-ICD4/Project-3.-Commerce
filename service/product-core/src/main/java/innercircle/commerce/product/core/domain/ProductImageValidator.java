package innercircle.commerce.product.core.domain;

import org.apache.commons.lang3.StringUtils;

public class ProductImageValidator {

	protected static void validateProductId (Long productId) {
		if (productId == null) {
			throw new IllegalArgumentException("상품 ID는 필수입니다.");
		}
	}

	protected static void validateUrl (String url) {
		if (StringUtils.isBlank(url)) {
			throw new IllegalArgumentException("이미지 URL은 필수입니다.");
		}
	}

	protected static void validateOriginalName (String originalName) {
		if (StringUtils.isBlank(originalName)) {
			throw new IllegalArgumentException("이미지 원본 파일명은 필수입니다.");
		}
	}

	protected static void validateSortOrder (int sortOrder) {
		if (sortOrder < 0) {
			throw new IllegalArgumentException("이미지 정렬 순서는 0 이상이어야 합니다.");
		}
	}
}