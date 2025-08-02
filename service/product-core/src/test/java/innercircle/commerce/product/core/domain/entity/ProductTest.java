package innercircle.commerce.product.core.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("상품 등록 도메인 테스트")
class ProductTest {
	private final String VALID_NAME = "테스트 상품";
	private final Long VALID_CATEGORY_ID = 2L;
	private final Long VALID_BRAND_ID = 2L;
	private final Integer VALID_BASE_PRICE = 10000;
	private final Integer VALID_STOCK = 100;
	private final String VALID_DETAIL_CONTENT = "\\<html\\>상품 설명\\<\\/html\\>";

	private final List<ProductImage> VALID_IMAGES = List.of(
			createProductImage("image1.jpg", true),   // 대표 이미지
			createProductImage("image2.jpg", false)
	);

	@Test
	@DisplayName("정상적인 상품 등록이 성공한다.")
	void 정상적인_상품_생성 () {
		// when & then
		assertThatNoException().isThrownBy(this::createValidProduct);
	}

	@Test
	@DisplayName("브랜드 ID가 null이면 예외가 발생한다.")
	void 브랜드가_null이면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			Product.create(VALID_NAME, VALID_CATEGORY_ID, null, VALID_BASE_PRICE, VALID_STOCK, VALID_IMAGES, VALID_DETAIL_CONTENT);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("브랜드 ID는 필수입니다");
	}

	@Test
	@DisplayName("최하위 카테고리 ID가 null이면 예외가 발생한다")
	void 카테고리가_null이면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			Product.create(VALID_NAME, null, VALID_BRAND_ID, VALID_BASE_PRICE, VALID_STOCK, VALID_IMAGES, VALID_DETAIL_CONTENT);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("최하위 카테고리 ID는 필수입니다");
	}

	@Test
	@DisplayName("가격이 음수이면 예외가 발생한다")
	void 가격이_음수면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			Product.create(VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, -1, VALID_STOCK, VALID_IMAGES, VALID_DETAIL_CONTENT);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 가격은 0 이상이어야 합니다");
	}

	@Test
	@DisplayName("재고가 음수이면 예외가 발생한다")
	void 재고가_음수면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			Product.create(VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, -1, VALID_IMAGES, VALID_DETAIL_CONTENT);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 재고는 0 이상이어야 합니다");
	}

	@Test
	@DisplayName("옵션이 존재하지만 옵션 속성이 없으면 예외가 발생한다")
	void 옵션이_존재하지만_속성이_없으면_예외_발생 () {
		List<ProductOption> options = List.of(
				createProductOptionWithoutItems("색상", true)
		);

		// when & then
		assertThatThrownBy(() -> {
			Product.createWithOptions(
					VALID_NAME,
					VALID_CATEGORY_ID,
					VALID_BRAND_ID,
					VALID_BASE_PRICE,
					VALID_STOCK,
					VALID_IMAGES,
					VALID_DETAIL_CONTENT,
					options);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("옵션이 존재하는 경우 최소 1개 이상의 옵션 속성이 필요합니다");
	}

	@Test
	@DisplayName("상품 이미지가 없으면 예외가 발생한다")
	void 상품_이미지가_없으면_예외_발생 () {
		List<ProductImage> images = Collections.emptyList();

		// when & then
		assertThatThrownBy(() -> {
			Product.create(VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, VALID_STOCK, images, VALID_DETAIL_CONTENT);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 이미지는 최소 1개 이상 필요합니다");
	}

	@Test
	@DisplayName("대표 이미지가 없으면 예외가 발생한다")
	void 대표_이미지가_없으면_예외_발생 () {
		List<ProductImage> images = List.of(
				createProductImage("image1.jpg", false),
				createProductImage("image2.jpg", false)
		);

		// when & then
		assertThatThrownBy(() -> {
			Product.create(VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, VALID_STOCK, images, VALID_DETAIL_CONTENT);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("대표 이미지가 반드시 1개 필요합니다");
	}

	@Test
	@DisplayName("상세 내용이 없으면 예외가 발생한다")
	void 상세_내용이_없으면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			Product.create(VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, VALID_STOCK, VALID_IMAGES, null);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 상세 내용은 필수입니다");
	}

	@Test
	@DisplayName("상품 생성 시 기본 상태는 SALE이다")
	void 상품_생성_상태는_SALE () {
		// when
		Product product = createValidProduct();

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE);
	}

	@Test
	@DisplayName("상품 생성 시 판매 타입 기본값이 NEW이다.")
	void 상품_생성_판매_타입은_NEW () {
		// when
		Product product = createValidProduct();

		// then
		assertThat(product.getSaleType()).isEqualTo(SaleType.NEW);
	}

	@Test
	@DisplayName("재고가 0인 상품의 재고를 늘리면 상태가 SALE로 변경된다.")
	void 상품_재고가_0이상이고_상태가_OUTOFSTOCK이면_SALE로_변경 () {
		Product product = Product.create(VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, 0, VALID_IMAGES, VALID_DETAIL_CONTENT);

		// when
		product.increaseStock(10);

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE);
		assertThat(product.getStock()).isEqualTo(10);
	}

	@Test
	@DisplayName("재고가 0으로 변경되면 상품의 상태가 OUTOFSTOCK으로 변경된다.")
	void 변경된_재고가_0이면_품절로_변경 () {
		Product product = Product.create(VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, 10, VALID_IMAGES, VALID_DETAIL_CONTENT);

		// when
		product.decreaseStock(10);

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.OUTOFSTOCK);
		assertThat(product.getStock()).isEqualTo(0);
	}

	private ProductImage createProductImage (String fileName, boolean isMain) {
		return ProductImage.builder()
						   .url("http://example.com/" + fileName)
						   .originalName(fileName)
						   .isMain(isMain)
						   .sortOrder(1)
						   .build();
	}

	private ProductOption createProductOptionWithoutItems (String optionName, boolean isRequired) {
		return ProductOption.builder()
							.name(optionName)
							.isRequired(isRequired)
							.sortOrder(1)
							.items(Collections.emptyList())  // 빈 리스트
							.build();
	}

	private Product createValidProduct () {
		return Product.create(
				VALID_NAME,
				VALID_CATEGORY_ID,
				VALID_BRAND_ID,
				VALID_BASE_PRICE,
				VALID_STOCK,
				VALID_IMAGES,
				VALID_DETAIL_CONTENT
		);
	}
}
