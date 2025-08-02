package innercircle.commerce.product.core.domain.entity;

import innercircle.commerce.product.core.fixtures.ProductFixtures;
import innercircle.commerce.product.core.fixtures.ProductImageFixtures;
import innercircle.commerce.product.core.fixtures.ProductOptionFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static innercircle.commerce.product.core.fixtures.ProductFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("상품 등록 도메인 테스트")
class ProductTest {

	@Test
	@DisplayName("정상적인 상품 등록이 성공한다.")
	void 정상적인_상품_생성 () {
		// when & then
		assertThatNoException().isThrownBy(ProductFixtures::createValidProduct);
	}

	@Test
	@DisplayName("브랜드 ID가 null이면 예외가 발생한다.")
	void 브랜드가_null이면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			ProductFixtures.createProductWithCustomValues(
					VALID_NAME, VALID_CATEGORY_ID, null, VALID_BASE_PRICE, VALID_STOCK,
					ProductImageFixtures.createValidImages(), VALID_DETAIL_CONTENT
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("브랜드 ID는 필수입니다");
	}

	@Test
	@DisplayName("최하위 카테고리 ID가 null이면 예외가 발생한다")
	void 카테고리가_null이면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			ProductFixtures.createProductWithCustomValues(
					VALID_NAME, null, VALID_BRAND_ID, VALID_BASE_PRICE, VALID_STOCK,
					ProductImageFixtures.createValidImages(), VALID_DETAIL_CONTENT
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("최하위 카테고리 ID는 필수입니다");
	}

	@Test
	@DisplayName("가격이 음수이면 예외가 발생한다")
	void 가격이_음수면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			ProductFixtures.createProductWithCustomValues(
					VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, -1, VALID_STOCK,
					ProductImageFixtures.createValidImages(), VALID_DETAIL_CONTENT
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 가격은 0 이상이어야 합니다");
	}

	@Test
	@DisplayName("재고가 음수이면 예외가 발생한다")
	void 재고가_음수면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			ProductFixtures.createProductWithCustomValues(
					VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, -1,
					ProductImageFixtures.createValidImages(), VALID_DETAIL_CONTENT
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 재고는 0 이상이어야 합니다");
	}

	@Test
	@DisplayName("옵션이 존재하지만 옵션 속성이 없으면 예외가 발생한다")
	void 옵션이_존재하지만_속성이_없으면_예외_발생 () {
		List<ProductOption> options = List.of(
				ProductOptionFixtures.createOptionWithoutItems("색상", true)
		);

		// when & then
		assertThatThrownBy(() -> {
			ProductFixtures.createProductWithOptions(options);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("옵션이 존재하는 경우 최소 1개 이상의 옵션 속성이 필요합니다");
	}

	@Test
	@DisplayName("상품 이미지가 없으면 예외가 발생한다")
	void 상품_이미지가_없으면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			ProductFixtures.createProductWithCustomValues(
					VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, VALID_STOCK,
					Collections.emptyList(), VALID_DETAIL_CONTENT
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 이미지는 최소 1개 이상 필요합니다");
	}

	@Test
	@DisplayName("대표 이미지가 없으면 예외가 발생한다")
	void 대표_이미지가_없으면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			ProductFixtures.createProductWithCustomValues(
					VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, VALID_STOCK,
					ProductImageFixtures.createImagesWithoutMain(), VALID_DETAIL_CONTENT
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("대표 이미지가 반드시 1개 필요합니다");
	}

	@Test
	@DisplayName("상세 내용이 없으면 예외가 발생한다")
	void 상세_내용이_없으면_예외_발생 () {
		// when & then
		assertThatThrownBy(() -> {
			ProductFixtures.createProductWithCustomValues(
					VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, VALID_STOCK,
					ProductImageFixtures.createValidImages(), null
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 상세 내용은 필수입니다");
	}

	@Test
	@DisplayName("상품 생성 시 기본 상태는 SALE이다")
	void 상품_생성_상태는_SALE () {
		// when
		Product product = ProductFixtures.createValidProduct();

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE);
	}

	@Test
	@DisplayName("상품 생성 시 판매 타입 기본값이 NEW이다.")
	void 상품_생성_판매_타입은_NEW () {
		// when
		Product product = ProductFixtures.createValidProduct();

		// then
		assertThat(product.getSaleType()).isEqualTo(SaleType.NEW);
	}

	@Test
	@DisplayName("재고가 0인 상품의 재고를 늘리면 상태가 SALE로 변경된다.")
	void 상품_재고가_0이상이고_상태가_OUTOFSTOCK이면_SALE로_변경 () {
		Product product = ProductFixtures.createProductWithCustomValues(
				VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, 0,
				ProductImageFixtures.createValidImages(), VALID_DETAIL_CONTENT
		);

		// when
		product.increaseStock(10);

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE);
		assertThat(product.getStock()).isEqualTo(10);
	}

	@Test
	@DisplayName("재고가 0으로 변경되면 상품의 상태가 OUTOFSTOCK으로 변경된다.")
	void 변경된_재고가_0이면_품절로_변경 () {
		Product product = ProductFixtures.createProductWithCustomValues(
				VALID_NAME, VALID_CATEGORY_ID, VALID_BRAND_ID, VALID_BASE_PRICE, 10,
				ProductImageFixtures.createValidImages(), VALID_DETAIL_CONTENT
		);

		// when
		product.decreaseStock(10);

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.OUTOFSTOCK);
		assertThat(product.getStock()).isEqualTo(0);
	}
}
