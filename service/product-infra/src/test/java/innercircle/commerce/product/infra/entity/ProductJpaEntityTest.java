package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProductJpaEntity 테스트")
class ProductJpaEntityTest {

	@Test
	@DisplayName("도메인 객체에서 JPA 엔티티로 변환할 수 있다.")
	void 도메인에서_엔티티_변환() {
		// given
		Product product = Product.create(
			"테스트 상품",
			1L,
			2L,
			10000,
			100,
			Collections.emptyList(),
			"상세 설명"
		);

		// when
		ProductJpaEntity jpaEntity = ProductJpaEntity.from(product);

		// then
		assertThat(jpaEntity.getId()).isEqualTo(product.getId());
		assertThat(jpaEntity.getName()).isEqualTo(product.getName());
		assertThat(jpaEntity.getCategoryId()).isEqualTo(product.getCategoryId());
		assertThat(jpaEntity.getBrandId()).isEqualTo(product.getBrandId());
		assertThat(jpaEntity.getPrice()).isEqualTo(product.getPrice());
		assertThat(jpaEntity.getStock()).isEqualTo(product.getStock());
		assertThat(jpaEntity.getStatus()).isEqualTo(product.getStatus());
		assertThat(jpaEntity.getSaleType()).isEqualTo(product.getSaleType());
		assertThat(jpaEntity.getDetailContent()).isEqualTo(product.getDetailContent());
	}

	@Test
	@DisplayName("JPA 엔티티에서 도메인 객체로 변환할 수 있다.")
	void 엔티티에서_도메인_변환() {
		// given
		ProductJpaEntity jpaEntity = ProductJpaEntity.from(
			Product.create(
				"테스트 상품",
				1L,
				2L,
				10000,
				100,
				Collections.emptyList(),
				"상세 설명"
			)
		);

		// when
		Product product = jpaEntity.toDomain();

		// then
		assertThat(product.getId()).isEqualTo(jpaEntity.getId());
		assertThat(product.getName()).isEqualTo(jpaEntity.getName());
		assertThat(product.getCategoryId()).isEqualTo(jpaEntity.getCategoryId());
		assertThat(product.getBrandId()).isEqualTo(jpaEntity.getBrandId());
		assertThat(product.getPrice()).isEqualTo(jpaEntity.getPrice());
		assertThat(product.getStock()).isEqualTo(jpaEntity.getStock());
		assertThat(product.getStatus()).isEqualTo(jpaEntity.getStatus());
		assertThat(product.getSaleType()).isEqualTo(jpaEntity.getSaleType());
		assertThat(product.getDetailContent()).isEqualTo(jpaEntity.getDetailContent());
	}

	@Test
	@DisplayName("도메인-엔티티 양방향 변환이 정확하다.")
	void 양방향_변환_일관성() {
		// given
		Product originalProduct = Product.create(
			"원본 상품",
			3L,
			4L,
			25000,
			50,
			Collections.emptyList(),
			"원본 상세 설명"
		);

		// when
		ProductJpaEntity jpaEntity = ProductJpaEntity.from(originalProduct);
		Product convertedProduct = jpaEntity.toDomain();

		// then
		assertThat(convertedProduct.getId()).isEqualTo(originalProduct.getId());
		assertThat(convertedProduct.getName()).isEqualTo(originalProduct.getName());
		assertThat(convertedProduct.getCategoryId()).isEqualTo(originalProduct.getCategoryId());
		assertThat(convertedProduct.getBrandId()).isEqualTo(originalProduct.getBrandId());
		assertThat(convertedProduct.getPrice()).isEqualTo(originalProduct.getPrice());
		assertThat(convertedProduct.getStock()).isEqualTo(originalProduct.getStock());
		assertThat(convertedProduct.getDetailContent()).isEqualTo(originalProduct.getDetailContent());
	}
}