package innercircle.commerce.product.core.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("상품 도메인 테스트")
class ProductTest {

	private List<ProductImage> createValidImages () {
		return List.of(
				ProductImage.builder()
							.url("https://example.com/main.jpg")
							.originalName("main.jpg")
							.isMain(true)
							.sortOrder(1)
							.build(),
				ProductImage.builder()
							.url("https://example.com/sub.jpg")
							.originalName("sub.jpg")
							.isMain(false)
							.sortOrder(2)
							.build()
		);
	}

	private Product createValidProduct () {
		return createProduct("테스트 상품", 1L, 1L, 10000, 100, null, createValidImages(), "<html>상세 내용</html>");
	}

	private Product createProductWithStock (int stock) {
		return createProduct("테스트 상품", 1L, 1L, 10000, stock, null, createValidImages(), "<html>상세 내용</html>");
	}

	private Product createProduct (String name, Long categoryId, Long brandId, Integer price, Integer stock,
			List<ProductOption> options, List<ProductImage> images, String detailContent) {
		return Product.create(name, categoryId, brandId, price, stock, options, images, detailContent);
	}

	private void assertProductCreationThrows (String name, Long categoryId, Long brandId, Integer price,
			Integer stock, List<ProductOption> options,
			List<ProductImage> images, String detailContent,
			String expectedMessage) {
		assertThatThrownBy(() -> createProduct(name, categoryId, brandId, price, stock, options, images, detailContent))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(expectedMessage);
	}

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
		assertProductCreationThrows(
				"테스트 상품", 1L, null, 10000, 100,
				null, createValidImages(), "<html>상세 내용</html>",
				"브랜드 ID는 필수입니다"
		);
	}

	@Test
	@DisplayName("최하위 카테고리 ID가 null이면 예외가 발생한다")
	void 카테고리가_null이면_예외_발생 () {
		// when & then
		assertProductCreationThrows(
				"테스트 상품", null, 1L, 10000, 100,
				null, createValidImages(), "<html>상세 내용</html>",
				"최하위 카테고리 ID는 필수입니다"
		);
	}

	@Test
	@DisplayName("가격이 음수이면 예외가 발생한다")
	void 가격이_음수면_예외_발생 () {
		// when & then
		assertProductCreationThrows(
				"테스트 상품", 1L, 1L, -1, 100,
				null, createValidImages(), "<html>상세 내용</html>",
				"상품 가격은 0 이상이어야 합니다"
		);
	}

	@Test
	@DisplayName("재고가 음수이면 예외가 발생한다")
	void 재고가_음수면_예외_발생 () {
		// when & then
		assertProductCreationThrows(
				"테스트 상품", 1L, 1L, 10000, -1,
				null, createValidImages(), "<html>상세 내용</html>",
				"상품 재고는 0 이상이어야 합니다"
		);
	}

	@Test
	@DisplayName("상품 이미지가 없으면 예외가 발생한다")
	void 상품_이미지가_없으면_예외_발생 () {
		// when & then
		assertProductCreationThrows(
				"테스트 상품", 1L, 1L, 10000, 100,
				null, Collections.emptyList(), "<html>상세 내용</html>",
				"상품 이미지는 최소 1개 이상 필요합니다"
		);
	}

	@Test
	@DisplayName("대표 이미지가 없으면 예외가 발생한다")
	void 대표_이미지가_없으면_예외_발생 () {
		List<ProductImage> imagesWithoutMain = List.of(
				ProductImage.builder()
							.url("https://example.com/sub1.jpg")
							.originalName("sub1.jpg")
							.isMain(false)
							.sortOrder(1)
							.build()
		);

		// when & then
		assertProductCreationThrows(
				"테스트 상품", 1L, 1L, 10000, 100,
				null, imagesWithoutMain, "<html>상세 내용</html>",
				"대표 이미지가 반드시 1개 필요합니다"
		);
	}

	@Test
	@DisplayName("상세 내용이 없으면 예외가 발생한다")
	void 상세_내용이_없으면_예외_발생 () {
		// when & then
		assertProductCreationThrows(
				"테스트 상품", 1L, 1L, 10000, 100,
				null, createValidImages(), null,
				"상품 상세 내용은 필수입니다"
		);
	}

	@Test
	@DisplayName("상품 생성 시 기본 상태는 재고에 따라 결정된다")
	void 상품_생성_상태는_재고에_따라_결정 () {
		// when - 재고가 있는 경우
		Product productWithStock = createProductWithStock(100);

		// when - 재고가 0인 경우
		Product productWithoutStock = createProductWithStock(0);

		// then
		assertThat(productWithStock.getStatus()).isEqualTo(ProductStatus.SALE);
		assertThat(productWithoutStock.getStatus()).isEqualTo(ProductStatus.OUTOFSTOCK);
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
	@DisplayName("재고 감소가 가능한지 검증할 수 있다.")
	void 재고_감소_가능_여부_검증 () {
		// given
		Product product = createProductWithStock(10);

		// when & then
		assertThat(product.canDecreaseStock(5)).isTrue();
		assertThat(product.canDecreaseStock(10)).isTrue();
		assertThat(product.canDecreaseStock(15)).isFalse();
		assertThat(product.canDecreaseStock(0)).isFalse();
		assertThat(product.canDecreaseStock(-1)).isFalse();
	}

	@Test
	@DisplayName("재고 증가가 가능한지 검증할 수 있다.")
	void 재고_증가_가능_여부_검증 () {
		// given
		Product product = createValidProduct();

		// when & then
		assertThat(product.canIncreaseStock(1)).isTrue();
		assertThat(product.canIncreaseStock(100)).isTrue();
		assertThat(product.canIncreaseStock(0)).isFalse();
		assertThat(product.canIncreaseStock(-1)).isFalse();
	}

	@Test
	@DisplayName("재고 수량에 따른 적절한 상태를 결정할 수 있다.")
	void 재고_수량에_따른_상태_결정 () {
		// given
		Product saleProduct = createProductWithStock(10);
		Product outOfStockProduct = createProductWithStock(0);

		// when & then
		// 재고가 0이면 OUTOFSTOCK
		assertThat(saleProduct.determineStatusByStock(0)).isEqualTo(ProductStatus.OUTOFSTOCK);

		// 재고가 있고 현재 OUTOFSTOCK이면 SALE로 변경
		assertThat(outOfStockProduct.determineStatusByStock(5)).isEqualTo(ProductStatus.SALE);

		// 재고가 있고 현재 SALE이면 SALE 유지
		assertThat(saleProduct.determineStatusByStock(5)).isEqualTo(ProductStatus.SALE);
	}

	@Test
	@DisplayName("상품을 판매 상태로 변경할 수 있다.")
	void 상품을_판매_상태로_변경 () {
		// given
		Product product = createValidProduct();

		// when
		product.changeToSale();

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE);
	}

	@Test
	@DisplayName("상품을 품절 상태로 변경할 수 있다.")
	void 상품을_품절_상태로_변경 () {
		// given
		Product product = createProductWithStock(0);

		// when
		product.changeToOutOfStock();

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.OUTOFSTOCK);
	}

	@Test
	@DisplayName("상품을 판매 중지 상태로 변경할 수 있다.")
	void 상품을_판매중지_상태로_변경 () {
		// given
		Product product = createValidProduct();

		// when
		product.changeToClose();

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.CLOSE);
	}

	@Test
	@DisplayName("판매 타입을 신상품으로 변경할 수 있다.")
	void 판매타입을_신상품으로_변경 () {
		// given
		Product product = createValidProduct();

		// when
		product.changeToNew();

		// then
		assertThat(product.getSaleType()).isEqualTo(SaleType.NEW);
	}

	@Test
	@DisplayName("판매 타입을 기존 상품으로 변경할 수 있다.")
	void 판매타입을_기존상품으로_변경 () {
		// given
		Product product = createValidProduct();

		// when
		product.changeToOld();

		// then
		assertThat(product.getSaleType()).isEqualTo(SaleType.OLD);
	}

	@Test
	@DisplayName("상품 기본 정보 수정이 성공한다.")
	void 상품_기본정보_수정_성공 () {
		// given
		Product product = createValidProduct();
		String newName = "수정된 상품명";
		Integer newPrice = 15000;
		String newDetailContent = "<html>수정된 상세 내용</html>";

		// when
		product.updateBasicInfo(newName, newPrice, newDetailContent);

		// then
		assertThat(product.getName()).isEqualTo(newName);
		assertThat(product.getBasePrice()).isEqualTo(newPrice);
		assertThat(product.getDetailContent()).isEqualTo(newDetailContent);
	}

	@Test
	@DisplayName("상품 이미지 수정이 성공한다.")
	void 상품_이미지_수정_성공 () {
		// given
		Product product = createValidProduct();
		List<ProductImage> newImages = List.of(
				ProductImage.builder()
							.url("https://example.com/new-main.jpg")
							.originalName("new-main.jpg")
							.isMain(true)
							.sortOrder(1)
							.build()
		);

		// when
		product.updateImages(newImages);

		// then
		assertThat(product.getImages()).hasSize(1);
		assertThat(product.getImages().getFirst().getOriginalName()).isEqualTo("new-main.jpg");
	}

	@Test
	@DisplayName("재고가 0인 상품을 SALE로 변경하면 예외가 발생한다.")
	void 재고가_0인_상품을_SALE로_변경_시_예외_발생 () {
		// given
		Product product = createProductWithStock(0);

		// when & then
		assertThatThrownBy(product::changeToSale)
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("재고가 0인 상품은 판매 상태로 변경할 수 없습니다");
	}

	@Test
	@DisplayName("재고가 있는 상품을 OUTOFSTOCK으로 변경하면 예외가 발생한다.")
	void 재고가_있는_상품을_OUTOFSTOCK으로_변경_시_예외_발생 () {
		// given
		Product product = createValidProduct();

		// when & then
		assertThatThrownBy(product::changeToOutOfStock)
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("재고가 있는 상품은 품절 상태로 변경할 수 없습니다");
	}
}