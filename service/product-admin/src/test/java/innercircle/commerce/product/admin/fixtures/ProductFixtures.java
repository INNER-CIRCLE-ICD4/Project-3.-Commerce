package innercircle.commerce.product.admin.fixtures;

import innercircle.commerce.product.core.domain.entity.Product;
import innercircle.commerce.product.core.domain.entity.ProductImage;
import innercircle.commerce.product.core.domain.entity.ProductOption;
import innercircle.commerce.product.core.domain.entity.ProductStatus;
import innercircle.commerce.product.core.domain.entity.SaleType;

import java.time.LocalDateTime;
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
				null, // 옵션 없음
				ProductImageFixtures.createValidImages(),
				VALID_DETAIL_CONTENT
		);
	}

	public static Product createValidProductWithOptions () {
		return Product.create(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				ProductOptionFixtures.createValidOptions(),
				ProductImageFixtures.createValidImages(),
				VALID_DETAIL_CONTENT
		);
	}

	public static Product createProductWithOptions (List<ProductOption> options) {
		return Product.create(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				options,
				ProductImageFixtures.createValidImages(),
				VALID_DETAIL_CONTENT
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
				null, // 옵션 없음
				images,
				detailContent
		);
	}

	/**
	 * 테스트용 - ID를 포함한 저장된 상품 객체를 생성합니다.
	 */
	public static Product createSavedProduct() {
		return Product.builder()
				.id(1L)
				.name(VALID_NAME)
				.code("PRD001")
				.leafCategoryId(VALID_CATEGORY_ID)
				.brandId(VALID_BRAND_ID)
				.basePrice(VALID_BASE_PRICE)
				.status(ProductStatus.SALE)
				.saleType(SaleType.NEW)
				.options(List.of())
				.images(ProductImageFixtures.createValidImages())
				.detailContent(VALID_DETAIL_CONTENT)
				.stock(VALID_STOCK)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();
	}
}
