package innercircle.commerce.product.admin.fixtures;

import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.dto.ProductImageInfo;

import java.util.List;

import static innercircle.commerce.product.admin.fixtures.ProductFixtures.*;

public class ProductCreateCommandFixtures {

	public static ProductCreateCommand createValidCommand () {
		return new ProductCreateCommand(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				VALID_DETAIL_CONTENT,
				null,
				createValidProductImageInfos()
		);
	}

	public static ProductCreateCommand createValidCommandWithOptions () {
		return new ProductCreateCommand(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				VALID_DETAIL_CONTENT,
				ProductOptionFixtures.createValidOptions(),
				createValidProductImageInfos()
		);
	}

	/**
	 * 예외 테스트용 - 최소한의 이미지 정보만 포함
	 */
	public static ProductCreateCommand createValidCommandForExceptionTest () {
		return new ProductCreateCommand(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				VALID_DETAIL_CONTENT,
				null,
				List.of(new ProductImageInfo(
						1234L,
						"original",
						"https://s3.amazonaws.com/bucket/commerce/temp/images/1234/original.jpg",
						true,
						1
				))
		);
	}

	private static List<ProductImageInfo> createValidProductImageInfos () {
		return List.of(
				new ProductImageInfo(
						1234L,
						"original",
						"https://s3.amazonaws.com/bucket/commerce/temp/images/1234/original.jpg",
						true,
						1
				),   // 메인 이미지
				new ProductImageInfo(
						2234L,
						"original",
						"https://s3.amazonaws.com/bucket/commerce/temp/images/2234/original.jpg",
						false,
						2
				)   // 서브 이미지
		);
	}
}
