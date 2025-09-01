package innercircle.commerce.product.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class ProductInventoryTest {

	@Nested
	@DisplayName("재고 증가")
	class IncreaseStock {

		@Test
		@DisplayName("유효한 수량으로 재고를 증가시킬 수 있다.")
		void 재고_증가_성공() {
			// given
			Product product = createTestProduct(10);
			LocalDateTime beforeUpdate = product.getUpdatedAt();

			// when
			product.increaseStock(5);

			// then
			assertThat(product.getStock()).isEqualTo(15);
			assertThat(product.getUpdatedAt()).isAfter(beforeUpdate);
		}

		@Test
		@DisplayName("null 수량으로 재고 증가를 시도하면 예외가 발생한다.")
		void 재고_증가_null_수량_예외() {
			// given
			Product product = createTestProduct(10);

			// when & then
			assertThatThrownBy(() -> product.increaseStock(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("증가할 재고 수량은 필수입니다.");
		}

		@Test
		@DisplayName("0 이하 수량으로 재고 증가를 시도하면 예외가 발생한다.")
		void 재고_증가_0이하_수량_예외() {
			// given
			Product product = createTestProduct(10);

			// when & then
			assertThatThrownBy(() -> product.increaseStock(0))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("증가할 재고 수량은 양수여야 합니다.");

			assertThatThrownBy(() -> product.increaseStock(-1))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("증가할 재고 수량은 양수여야 합니다.");
		}
	}

	@Nested
	@DisplayName("재고 감소")
	class DecreaseStock {

		@Test
		@DisplayName("유효한 수량으로 재고를 감소시킬 수 있다.")
		void 재고_감소_성공() {
			// given
			Product product = createTestProduct(10);
			LocalDateTime beforeUpdate = product.getUpdatedAt();

			// when
			product.decreaseStock(3);

			// then
			assertThat(product.getStock()).isEqualTo(7);
			assertThat(product.getUpdatedAt()).isAfter(beforeUpdate);
		}

		@Test
		@DisplayName("전체 재고를 모두 감소시킬 수 있다.")
		void 전체_재고_감소_성공() {
			// given
			Product product = createTestProduct(5);

			// when
			product.decreaseStock(5);

			// then
			assertThat(product.getStock()).isEqualTo(0);
		}

		@Test
		@DisplayName("null 수량으로 재고 감소를 시도하면 예외가 발생한다.")
		void 재고_감소_null_수량_예외() {
			// given
			Product product = createTestProduct(10);

			// when & then
			assertThatThrownBy(() -> product.decreaseStock(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("감소할 재고 수량은 필수입니다.");
		}

		@Test
		@DisplayName("0 이하 수량으로 재고 감소를 시도하면 예외가 발생한다.")
		void 재고_감소_0이하_수량_예외() {
			// given
			Product product = createTestProduct(10);

			// when & then
			assertThatThrownBy(() -> product.decreaseStock(0))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("감소할 재고 수량은 양수여야 합니다.");

			assertThatThrownBy(() -> product.decreaseStock(-1))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("감소할 재고 수량은 양수여야 합니다.");
		}

		@Test
		@DisplayName("재고보다 많은 수량을 감소시키려 하면 예외가 발생한다.")
		void 재고_부족_예외() {
			// given
			Product product = createTestProduct(5);

			// when & then
			assertThatThrownBy(() -> product.decreaseStock(10))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("재고가 부족합니다. 현재 재고: 5, 요청 수량: 10");
		}
	}

	private Product createTestProduct(Integer stock) {
		LocalDateTime now = LocalDateTime.now();
		return Product.restore(
				1L,
				"테스트 상품",
				1L,
				1L,
				10000,
				stock,
				1L,
				null,
				null,
				"테스트 상품 설명",
				SaleType.NEW,
				ProductStatus.SALE,
				now,
				now
		);
	}
}