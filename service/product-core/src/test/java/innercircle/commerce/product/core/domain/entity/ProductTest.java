package innercircle.commerce.product.core.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("상품 도메인 테스트")
class ProductTest {

	private List<ProductImage> createValidImages() {
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

	private Product createValidProduct() {
		return Product.create(
			"테스트 상품", 1L, 1L, 10000, 100,
			createValidImages(), "<html>상세 내용</html>"
		);
	}

	@Test
	@DisplayName("정상적인 상품 등록이 성공한다.")
	void 정상적인_상품_생성() {
		// when & then
		assertThatNoException().isThrownBy(() -> createValidProduct());
	}

	@Test
	@DisplayName("브랜드 ID가 null이면 예외가 발생한다.")
	void 브랜드가_null이면_예외_발생() {
		// when & then
		assertThatThrownBy(() -> {
			Product.create(
				"테스트 상품", 1L, null, 10000, 100,
				createValidImages(), "<html>상세 내용</html>"
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("브랜드 ID는 필수입니다");
	}

	@Test
	@DisplayName("최하위 카테고리 ID가 null이면 예외가 발생한다")
	void 카테고리가_null이면_예외_발생() {
		// when & then
		assertThatThrownBy(() -> {
			Product.create(
				"테스트 상품", null, 1L, 10000, 100,
				createValidImages(), "<html>상세 내용</html>"
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("최하위 카테고리 ID는 필수입니다");
	}

	@Test
	@DisplayName("가격이 음수이면 예외가 발생한다")
	void 가격이_음수면_예외_발생() {
		// when & then
		assertThatThrownBy(() -> {
			Product.create(
				"테스트 상품", 1L, 1L, -1, 100,
				createValidImages(), "<html>상세 내용</html>"
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 가격은 0 이상이어야 합니다");
	}

	@Test
	@DisplayName("재고가 음수이면 예외가 발생한다")
	void 재고가_음수면_예외_발생() {
		// when & then
		assertThatThrownBy(() -> {
			Product.create(
				"테스트 상품", 1L, 1L, 10000, -1,
				createValidImages(), "<html>상세 내용</html>"
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 재고는 0 이상이어야 합니다");
	}

	@Test
	@DisplayName("상품 이미지가 없으면 예외가 발생한다")
	void 상품_이미지가_없으면_예외_발생() {
		// when & then
		assertThatThrownBy(() -> {
			Product.create(
				"테스트 상품", 1L, 1L, 10000, 100,
				Collections.emptyList(), "<html>상세 내용</html>"
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 이미지는 최소 1개 이상 필요합니다");
	}

	@Test
	@DisplayName("대표 이미지가 없으면 예외가 발생한다")
	void 대표_이미지가_없으면_예외_발생() {
		List<ProductImage> imagesWithoutMain = List.of(
			ProductImage.builder()
				.url("https://example.com/sub1.jpg")
				.originalName("sub1.jpg")
				.isMain(false)
				.sortOrder(1)
				.build()
		);

		// when & then
		assertThatThrownBy(() -> {
			Product.create(
				"테스트 상품", 1L, 1L, 10000, 100,
				imagesWithoutMain, "<html>상세 내용</html>"
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("대표 이미지가 반드시 1개 필요합니다");
	}

	@Test
	@DisplayName("상세 내용이 없으면 예외가 발생한다")
	void 상세_내용이_없으면_예외_발생() {
		// when & then
		assertThatThrownBy(() -> {
			Product.create(
				"테스트 상품", 1L, 1L, 10000, 100,
				createValidImages(), null
			);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("상품 상세 내용은 필수입니다");
	}

	@Test
	@DisplayName("상품 생성 시 기본 상태는 SALE이다")
	void 상품_생성_상태는_SALE() {
		// when
		Product product = createValidProduct();

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE);
	}

	@Test
	@DisplayName("상품 생성 시 판매 타입 기본값이 NEW이다.")
	void 상품_생성_판매_타입은_NEW() {
		// when
		Product product = createValidProduct();

		// then
		assertThat(product.getSaleType()).isEqualTo(SaleType.NEW);
	}

	@Test
	@DisplayName("재고 감소 시 재고가 0이 되면 상태가 OUTOFSTOCK으로 변경된다.")
	void 재고_감소로_0이_되면_품절_상태로_변경() {
		// given
		Product product = Product.create(
			"테스트 상품", 1L, 1L, 10000, 10,
			createValidImages(), "<html>상세 내용</html>"
		);

		// when
		product.decreaseStock(10);

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.OUTOFSTOCK);
		assertThat(product.getStock()).isEqualTo(0);
	}

	@Test
	@DisplayName("재고가 0인 상품의 재고를 늘리면 상태가 SALE로 변경된다.")
	void 재고_증가로_품절에서_판매_상태로_변경() {
		// given
		Product product = Product.create(
			"테스트 상품", 1L, 1L, 10000, 0,
			createValidImages(), "<html>상세 내용</html>"
		);
		
		// when
		product.increaseStock(10);

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE);
		assertThat(product.getStock()).isEqualTo(10);
	}

	@Test
	@DisplayName("상품 기본 정보 수정이 성공한다.")
	void 상품_기본정보_수정_성공() {
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
	void 상품_이미지_수정_성공() {
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
		assertThat(product.getImages().get(0).getOriginalName()).isEqualTo("new-main.jpg");
	}

	@Test
	@DisplayName("재고가 0인 상품을 SALE로 변경하면 예외가 발생한다.")
	void 재고가_0인_상품을_SALE로_변경_시_예외_발생() {
		// given
		Product product = Product.create(
			"테스트 상품", 1L, 1L, 10000, 0,
			createValidImages(), "<html>상세 내용</html>"
		);

		// when & then
		assertThatThrownBy(() -> {
			product.changeStatus(ProductStatus.SALE);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("재고가 0인 상품은 판매 상태로 변경할 수 없습니다");
	}

	@Test
	@DisplayName("재고가 있는 상품을 OUTOFSTOCK으로 변경하면 예외가 발생한다.")
	void 재고가_있는_상품을_OUTOFSTOCK으로_변경_시_예외_발생() {
		// given
		Product product = createValidProduct();

		// when & then
		assertThatThrownBy(() -> {
			product.changeStatus(ProductStatus.OUTOFSTOCK);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("재고가 있는 상품은 품절 상태로 변경할 수 없습니다");
	}
}