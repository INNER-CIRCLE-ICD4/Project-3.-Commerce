package innercircle.commerce.product.core.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class ProductValidator {

	private ProductValidator () {
	}

	protected static void validateProductName (String name) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("상품명은 필수입니다");
		}
	}

	protected static void validateCategoryId (Long categoryId) {
		if (categoryId == null) {
			throw new IllegalArgumentException("최하위 카테고리 ID는 필수입니다");
		}
	}

	protected static void validateBrandId (Long brandId) {
		if (brandId == null) {
			throw new IllegalArgumentException("브랜드 ID는 필수입니다");
		}
	}

	protected static void validatePrice (Integer price) {
		if (price == null || price < 0) {
			throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다");
		}
	}

	protected static void validateStock (Integer stock) {
		if (stock == null || stock < 0) {
			throw new IllegalArgumentException("상품 재고는 0 이상이어야 합니다");
		}
	}

	protected static void validateDetailContent (String detailContent) {
		if (StringUtils.isBlank(detailContent)) {
			throw new IllegalArgumentException("상품 상세 내용은 필수입니다");
		}
	}

	protected static void validateOptions (List<ProductOption> options) {
		if (options != null && options.size() > 10) {
			throw new IllegalArgumentException("상품 옵션은 10개까지 등록할 수 있습니다.");
		}
	}

	protected static void validateImages (List<ProductImage> images) {
		if (CollectionUtils.isEmpty(images)) {
			throw new IllegalArgumentException("상품 이미지는 필수 입니다.");
		}

		if (images.size() > 6) {
			throw new IllegalArgumentException("상품 이미지는 6개까지 등록할 수 있습니다.");
		}

		boolean hasMainImage = images.stream().anyMatch(ProductImage::isMain);
		if (!hasMainImage) {
			throw new IllegalArgumentException("메인 이미지는 필수 입니다.");
		}
	}
}
