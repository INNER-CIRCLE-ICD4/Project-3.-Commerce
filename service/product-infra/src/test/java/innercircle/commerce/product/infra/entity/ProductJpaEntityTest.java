package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductJpaEntity 테스트")
class ProductJpaEntityTest {

	@Mock
	private BrandJpaEntity brand;
	@Mock
	private CategoryJpaEntity category;

	@Test
	@DisplayName("도메인 객체에서 JPA 엔티티로 변환할 수 있다.")
	void 도메인에서_엔티티_변환() {
		// given
		Product product = Product.create(
			"테스트 상품",
			1L, // categoryId
			2L, // brandId
			10000,
			100,
			Collections.emptyList(),
			"상세 설명"
		);

		// when
		ProductJpaEntity jpaEntity = ProductJpaEntity.from(product, category, brand);

		// then
		assertThat(jpaEntity.getId()).isEqualTo(product.getId());
		assertThat(jpaEntity.getName()).isEqualTo(product.getName());
		assertThat(jpaEntity.getCategory()).isEqualTo(category);
		assertThat(jpaEntity.getBrand()).isEqualTo(brand);
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
		given(category.getId()).willReturn(1L);
		given(brand.getId()).willReturn(2L);
		Product productDomain = Product.create(
				"테스트 상품", 1L, 2L, 10000, 100, Collections.emptyList(), "상세 설명");
		ProductJpaEntity jpaEntity = ProductJpaEntity.from(productDomain, category, brand);

		// when
		Product product = jpaEntity.toDomain();

		// then
		assertThat(product.getId()).isEqualTo(jpaEntity.getId());
		assertThat(product.getName()).isEqualTo(jpaEntity.getName());
		assertThat(product.getCategoryId()).isEqualTo(jpaEntity.getCategory().getId());
		assertThat(product.getBrandId()).isEqualTo(jpaEntity.getBrand().getId());
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
		// Use mocks for category and brand IDs to match the setup
		given(category.getId()).willReturn(3L); // Stub category ID for this test
		given(brand.getId()).willReturn(4L);     // Stub brand ID for this test

		Product originalProduct = Product.create(
			"원본 상품",
			category.getId(), // Use stubbed ID
			brand.getId(),    // Use stubbed ID
			25000,
			50,
			Collections.emptyList(),
			"원본 상세 설명"
		);

		// when
		ProductJpaEntity jpaEntity = ProductJpaEntity.from(originalProduct, category, brand);
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
