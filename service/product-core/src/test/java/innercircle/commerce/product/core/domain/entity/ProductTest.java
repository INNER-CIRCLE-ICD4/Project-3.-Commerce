package innercircle.commerce.product.core.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Product 도메인 테스트")
class ProductTest {

	@Nested
	@DisplayName("상품 생성 테스트")
	class CreateProductTest {

		@Test
		@DisplayName("유효한 정보로 상품을 생성할 수 있다.")
		void createProductWithValidInfo () {
			// given
			String name = "테스트 상품";
			Long categoryId = 1L;
			Long brandId = 1L;
			Integer basePrice = 10000;

			// when
			Product product = Product.create(name, categoryId, brandId, basePrice);

			// then
			assertThat(product).isNotNull();
			assertThat(product.getName()).isEqualTo(name);
			assertThat(product.getCategoryId()).isEqualTo(categoryId);
			assertThat(product.getBrandId()).isEqualTo(brandId);
			assertThat(product.getBasePrice()).isEqualTo(basePrice);
			assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
		}

		@Test
		@DisplayName("상품명이 null이면 예외가 발생한다.")
		void createProductWithNullName () {
			// given
			String name = null;
			Long categoryId = 1L;
			Long brandId = 1L;
			Integer basePrice = 10000;

			// when & then
			assertThatThrownBy(() -> Product.create(name, categoryId, brandId, basePrice))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("상품명은 필수입니다.");
		}

		@Test
		@DisplayName("상품명이 빈 문자열이면 예외가 발생한다")
		void createProductWithEmptyName () {
			// given
			String name = "";
			Long categoryId = 1L;
			Long brandId = 1L;
			Integer basePrice = 10000;

			// when & then
			assertThatThrownBy(() -> Product.create(name, categoryId, brandId, basePrice))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("상품명은 필수입니다.");
		}

		@Test
		@DisplayName("상품명이 1자이면 예외가 발생한다")
		void createProductWithOneCharacterName () {
			// given
			String name = "A";
			Long categoryId = 1L;
			Long brandId = 1L;
			Integer basePrice = 10000;

			// when & then
			assertThatThrownBy(() -> Product.create(name, categoryId, brandId, basePrice))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("상품명은 2자 이상 50자 이하여야 합니다.");
		}

		@Test
		@DisplayName("상품명이 50자를 초과하면 예외가 발생한다")
		void createProductWithTooLongName () {
			// given
			String name = "A".repeat(51); // 51자
			Long categoryId = 1L;
			Long brandId = 1L;
			Integer basePrice = 10000;

			// when & then
			assertThatThrownBy(() -> Product.create(name, categoryId, brandId, basePrice))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("상품명은 2자 이상 50자 이하여야 합니다.");
		}

		@ParameterizedTest
		@ValueSource(strings = {"테스트", "상품명입니다", "Test Product"})
		@DisplayName("상품명이 2~50자면 정상적으로 생성된다.")
		void createProductWithValidNameLength (String name) {
			// given
			Long categoryId = 1L;
			Long brandId = 1L;
			Integer basePrice = 10000;

			// when
			Product product = Product.create(name, categoryId, brandId, basePrice);

			// then
			assertThat(product.getName()).isEqualTo(name);
		}

		@Test
		@DisplayName("카테고리 ID가 null이면 예외가 발생한다")
		void createProductWithNullCategoryId () {
			// given
			String name = "테스트 상품";
			Long categoryId = null;
			Long brandId = 1L;
			Integer basePrice = 10000;

			// when & then
			assertThatThrownBy(() -> Product.create(name, categoryId, brandId, basePrice))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("카테고리 ID는 필수입니다.");
		}

		@Test
		@DisplayName("브랜드 ID가 null이면 예외가 발생한다")
		void createProductWithNullBrandId () {
			// given
			String name = "테스트 상품";
			Long categoryId = 1L;
			Long brandId = null;
			Integer basePrice = 10000;

			// when & then
			assertThatThrownBy(() -> Product.create(name, categoryId, brandId, basePrice))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("브랜드 ID는 필수입니다.");
		}

		@Test
		@DisplayName("가격이 null이면 예외가 발생한다")
		void createProductWithNullPrice () {
			// given
			String name = "테스트 상품";
			Long categoryId = 1L;
			Long brandId = 1L;
			Integer basePrice = null;

			// when & then
			assertThatThrownBy(() -> Product.create(name, categoryId, brandId, basePrice))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("가격은 필수입니다.");
		}

		@Test
		@DisplayName("가격이 음수이면 예외가 발생한다")
		void createProductWithNegativePrice () {
			// given
			String name = "테스트 상품";
			Long categoryId = 1L;
			Long brandId = 1L;
			Integer basePrice = -1000;

			// when & then
			assertThatThrownBy(() -> Product.create(name, categoryId, brandId, basePrice))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("가격은 0 이상이어야 합니다.");
		}

		@ParameterizedTest
		@ValueSource(ints = {0, 1, 1000, 999999})
		@DisplayName("가격이 0 이상이면 정상적으로 생성된다")
		void createProductWithValidPrice (Integer basePrice) {
			// given
			String name = "테스트 상품";
			Long categoryId = 1L;
			Long brandId = 1L;

			// when
			Product product = Product.create(name, categoryId, brandId, basePrice);

			// then
			assertThat(product.getBasePrice()).isEqualTo(basePrice);
		}
	}

	@Nested
	@DisplayName("상품 상태 변경 테스트")
	class ChangeStatusTest {

		@Test
		@DisplayName("상품 상태를 INACTIVE로 변경할 수 있다")
		void changeStatusToInactive () {
			// given
			Product product = createValidProduct();

			// when
			product.changeStatus(ProductStatus.INACTIVE);

			// then
			assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
		}

		@Test
		@DisplayName("상품 상태를 SOLD_OUT으로 변경할 수 있다")
		void changeStatusToSoldOut () {
			// given
			Product product = createValidProduct();

			// when
			product.changeStatus(ProductStatus.SOLD_OUT);

			// then
			assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
		}

		@Test
		@DisplayName("상품 상태가 null이면 예외가 발생한다")
		void changeStatusWithNull () {
			// given
			Product product = createValidProduct();

			// when & then
			assertThatThrownBy(() -> product.changeStatus(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("상품 상태는 필수입니다.");
		}
	}

	@Nested
	@DisplayName("상품 정보 업데이트 테스트")
	class UpdateProductTest {

		@Test
		@DisplayName("유효한 정보로 상품을 업데이트할 수 있다")
		void updateProductWithValidInfo () {
			// given
			Product product = createValidProduct();
			String newName = "업데이트된 상품";
			Long newCategoryId = 2L;
			Long newBrandId = 2L;
			Integer newPrice = 20000;

			// when
			product.update(newName, newCategoryId, newBrandId, newPrice);

			// then
			assertThat(product.getName()).isEqualTo(newName);
			assertThat(product.getCategoryId()).isEqualTo(newCategoryId);
			assertThat(product.getBrandId()).isEqualTo(newBrandId);
			assertThat(product.getBasePrice()).isEqualTo(newPrice);
		}

		@Test
		@DisplayName("업데이트 시 상품명이 null이면 예외가 발생한다")
		void updateProductWithNullName () {
			// given
			Product product = createValidProduct();

			// when & then
			assertThatThrownBy(() -> product.update(null, 2L, 2L, 20000))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("상품명은 필수입니다.");
		}

		@Test
		@DisplayName("업데이트 시 가격이 음수이면 예외가 발생한다")
		void updateProductWithNegativePrice () {
			// given
			Product product = createValidProduct();

			// when & then
			assertThatThrownBy(() -> product.update("업데이트된 상품", 2L, 2L, -1000))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("가격은 0 이상이어야 합니다.");
		}
	}

	private Product createValidProduct () {
		return Product.create("테스트 상품", 1L, 1L, 10000);
	}
}
