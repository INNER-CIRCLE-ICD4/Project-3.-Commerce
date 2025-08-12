package innercircle.commerce.product.admin.fixtures;

import innercircle.commerce.product.admin.web.dto.ProductCreateRequest;
import innercircle.commerce.product.admin.web.dto.ProductImageRequest;

import java.util.List;

import static innercircle.commerce.product.admin.fixtures.ProductFixtures.*;

public class ProductCreateRequestFixtures {

	public static ProductCreateRequest createValidRequest() {
		return new ProductCreateRequest(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				VALID_DETAIL_CONTENT,
				null,
				createValidImageRequests()
		);
	}

	public static ProductCreateRequest createRequestWithOptions() {
		return new ProductCreateRequest(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				VALID_DETAIL_CONTENT,
				ProductOptionFixtures.createValidOptions(),
				createValidImageRequests()
		);
	}

	public static ProductCreateRequest createInvalidRequestWithoutName() {
		return new ProductCreateRequest(
				null, // 상품명 없음
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				VALID_DETAIL_CONTENT,
				null,
				createValidImageRequests()
		);
	}

	public static ProductCreateRequest createInvalidRequestWithoutImages() {
		return new ProductCreateRequest(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				VALID_DETAIL_CONTENT,
				null,
				List.of() // 이미지 없음
		);
	}

	public static ProductCreateRequest createInvalidRequestWithNegativePrice() {
		return new ProductCreateRequest(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				-1000, // 음수 가격
				VALID_STOCK,
				VALID_DETAIL_CONTENT,
				null,
				createValidImageRequests()
		);
	}

	private static List<ProductImageRequest> createValidImageRequests() {
		return List.of(
				new ProductImageRequest(
						1234L,
						"main-image.jpg",
						"https://s3.amazonaws.com/bucket/commerce/temp/images/1234/original.jpg",
						true,  // 대표 이미지
						1
				),
				new ProductImageRequest(
						2234L,
						"sub-image.jpg", 
						"https://s3.amazonaws.com/bucket/commerce/temp/images/2234/original.jpg",
						false,  // 서브 이미지
						2
				)
		);
	}
}