package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.core.application.repository.BrandRepository;
import innercircle.commerce.product.core.application.repository.CategoryRepository;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Brand;
import innercircle.commerce.product.core.domain.Category;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import innercircle.commerce.product.infra.config.InfraConfig;
import innercircle.commerce.product.infra.entity.ProductJpaEntity;
import innercircle.commerce.product.infra.repository.BrandRepositoryAdapter;
import innercircle.commerce.product.infra.repository.CategoryRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({InfraConfig.class, ProductRepositoryImpl.class, BrandRepositoryAdapter.class, CategoryRepositoryAdapter.class})
@ActiveProfiles("test")
class ProductRepositoryImplTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private BrandRepository brandRepository;
	@Autowired
	private CategoryRepository categoryRepository;

	private Brand savedBrand;
	private Category savedCategory;

	@BeforeEach
	void setUp () {
		Brand brand = Brand.create("Test Brand");
		Category category = Category.create("Test Category");
		savedBrand = brandRepository.save(brand);
		savedCategory = categoryRepository.save(category);
		entityManager.flush();
		entityManager.clear();
	}

	private Product createAndSaveProduct(String name) {
		Product product = Product.create(
				name,
				savedCategory.getId(),
				savedBrand.getId(),
				15000,
				100,
				Collections.emptyList(),
				"상품 상세 설명"
		);
		return productRepository.save(product);
	}

	@Nested
	@DisplayName("상품 저장")
	class Save {

		@Test
		@DisplayName("새로운 상품을 저장할 수 있다.")
		void 새_상품_저장 () {
			// given
			Product product = Product.create(
					"테스트 상품",
					savedCategory.getId(),
					savedBrand.getId(),
					15000,
					100,
					Collections.emptyList(),
					"상품 상세 설명"
			);

			// when
			Product savedProduct = productRepository.save(product);

			// then
			assertThat(savedProduct).isNotNull();
			assertThat(savedProduct.getId()).isNotNull();
			assertThat(savedProduct.getName()).isEqualTo("테스트 상품");
			assertThat(savedProduct.getCategoryId()).isEqualTo(savedCategory.getId());
			assertThat(savedProduct.getBrandId()).isEqualTo(savedBrand.getId());
			assertThat(savedProduct.getPrice()).isEqualTo(15000);
			assertThat(savedProduct.getStock()).isEqualTo(100);
			assertThat(savedProduct.getDetailContent()).isEqualTo("상품 상세 설명");
			assertThat(savedProduct.getStatus()).isEqualTo(ProductStatus.SALE);
			assertThat(savedProduct.getSaleType()).isEqualTo(SaleType.NEW);

			ProductJpaEntity foundEntity = entityManager.find(ProductJpaEntity.class, savedProduct.getId());
			assertThat(foundEntity).isNotNull();
			assertThat(foundEntity.getName()).isEqualTo("테스트 상품");
			assertThat(foundEntity.getCategory().getId()).isEqualTo(savedCategory.getId());
			assertThat(foundEntity.getBrand().getId()).isEqualTo(savedBrand.getId());
		}

		@Test
		@DisplayName("기존 상품을 업데이트할 수 있다.")
		void 기존_상품_업데이트 () {
			// given
			Product savedProduct = createAndSaveProduct("원본 상품");
			entityManager.flush();
			entityManager.clear();

			savedProduct.update("수정된 상품", 20000, "수정된 설명");

			// when
			Product updatedProduct = productRepository.save(savedProduct);

			// then
			assertThat(updatedProduct.getId()).isEqualTo(savedProduct.getId());
			assertThat(updatedProduct.getName()).isEqualTo("수정된 상품");
			assertThat(updatedProduct.getPrice()).isEqualTo(20000);
			assertThat(updatedProduct.getDetailContent()).isEqualTo("수정된 설명");

			ProductJpaEntity foundEntity = entityManager.find(ProductJpaEntity.class, updatedProduct.getId());
			assertThat(foundEntity.getName()).isEqualTo("수정된 상품");
			assertThat(foundEntity.getPrice()).isEqualTo(20000);
		}
	}

	@Nested
	@DisplayName("상품 조회")
	class FindById {

		@Test
		@DisplayName("존재하는 상품을 ID로 조회할 수 있다.")
		void 존재하는_상품_ID_조회 () {
			// given
			Product savedProduct = createAndSaveProduct("조회 테스트 상품");
			entityManager.flush();
			entityManager.clear();

			// when
			Optional<Product> foundProductOpt = productRepository.findById(savedProduct.getId());

			// then
			assertThat(foundProductOpt).isPresent();
			Product foundProduct = foundProductOpt.get();
			assertThat(foundProduct.getId()).isEqualTo(savedProduct.getId());
			assertThat(foundProduct.getName()).isEqualTo("조회 테스트 상품");
			assertThat(foundProduct.getCategoryId()).isEqualTo(savedCategory.getId());
			assertThat(foundProduct.getBrandId()).isEqualTo(savedBrand.getId());
		}

		@Test
		@DisplayName("존재하지 않는 상품을 ID로 조회하면 빈 Optional을 반환한다.")
		void 존재하지_않는_상품_ID_조회 () {
			// when
			Optional<Product> foundProduct = productRepository.findById(999999L);

			// then
			assertThat(foundProduct).isEmpty();
		}
	}

	@Nested
	@DisplayName("상품명 존재 확인")
	class ExistsByName {

		@Test
		@DisplayName("존재하는 상품명인 경우 true를 반환한다.")
		void 존재하는_상품명_확인 () {
			// given
			createAndSaveProduct("중복 체크 상품");
			entityManager.flush();

			// when
			boolean exists = productRepository.existsByName("중복 체크 상품");

			// then
			assertThat(exists).isTrue();
		}
	}

	@Nested
	@DisplayName("상품명 중복 확인 (ID 제외)")
	class ExistsByNameAndIdNot {

		@Test
		@DisplayName("특정 ID를 제외하고 같은 이름의 상품이 존재하는 경우 true를 반환한다.")
		void 특정_ID_제외_중복_상품명_존재 () {
			// given
			createAndSaveProduct("동일 이름 상품");
			Product savedProduct2 = createAndSaveProduct("동일 이름 상품");
			entityManager.flush();

			// when
			boolean exists = productRepository.existsByNameAndIdNot("동일 이름 상품", savedProduct2.getId());

			// then
			assertThat(exists).isTrue();
		}
	}
}
