package innercircle.commerce.product.core.fixtures;

import innercircle.commerce.product.core.domain.entity.Product;
import innercircle.commerce.product.core.domain.entity.ProductImage;
import innercircle.commerce.product.core.domain.entity.ProductOption;

import java.util.List;

public class ProductFixtures {

	public static final String VALID_NAME = "테스트 상품";
	public static final Long VALID_CATEGORY_ID = 2L;
	public static final Long VALID_BRAND_ID = 2L;
	public static final Integer VALID_BASE_PRICE = 10000;
	public static final Integer VALID_STOCK = 100;
	public static final String VALID_DETAIL_CONTENT = "<html>상품 설명</html>";

	public static Product createValidProduct () {
		return Product.create(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				ProductImageFixtures.createValidImages(),
				VALID_DETAIL_CONTENT
		);
	}

	public static Product createValidProductWithOptions () {
		return Product.createWithOptions(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				ProductImageFixtures.createValidImages(),
				VALID_DETAIL_CONTENT,
				ProductOptionFixtures.createValidOptions()
		);
	}

	public static Product createProductWithOptions (List<ProductOption> options) {
		return Product.createWithOptions(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				ProductImageFixtures.createValidImages(),
				VALID_DETAIL_CONTENT,
				options
		);
	}

	public static Product createProductWithCustomValues (
			String name,
			Long categoryId,
			Long brandId,
			Integer basePrice,
			Integer stock,
			List<ProductImage> images,
			String detailContent
	) {
		return Product.create(
				name,
				categoryId,
				brandId,
				basePrice,
				stock,
				images,
				detailContent
		);
	}
}
