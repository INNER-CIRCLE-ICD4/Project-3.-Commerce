package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;
import innercircle.commerce.product.infra.config.InfraConfig;
import innercircle.commerce.product.infra.entity.ProductJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({InfraConfig.class})
class ProductRepositoryImplTest {

	@Autowired
	private ProductJpaRepository productJpaRepository;

	@Autowired
	private TestEntityManager entityManager;

	private ProductRepository productRepository;

	@BeforeEach
	void setUp () {
		productRepository = new ProductRepositoryImpl(productJpaRepository);
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
					1L,
					2L,
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
			assertThat(savedProduct.getCategoryId()).isEqualTo(1L);
			assertThat(savedProduct.getBrandId()).isEqualTo(2L);
			assertThat(savedProduct.getPrice()).isEqualTo(15000);
			assertThat(savedProduct.getStock()).isEqualTo(100);
			assertThat(savedProduct.getDetailContent()).isEqualTo("상품 상세 설명");
			assertThat(savedProduct.getStatus()).isEqualTo(ProductStatus.SALE);
			assertThat(savedProduct.getSaleType()).isEqualTo(SaleType.NEW);

			// 데이터베이스에 실제로 저장되었는지 확인
			ProductJpaEntity foundEntity = entityManager.find(ProductJpaEntity.class, savedProduct.getId());
			assertThat(foundEntity).isNotNull();
			assertThat(foundEntity.getName()).isEqualTo("테스트 상품");
		}

		@Test
		@DisplayName("기존 상품을 업데이트할 수 있다.")
		void 기존_상품_업데이트 () {
			// given - 먼저 상품을 저장
			Product originalProduct = Product.create(
					"원본 상품",
					1L,
					2L,
					10000,
					50,
					Collections.emptyList(),
					"원본 설명"
			);
			Product savedProduct = productRepository.save(originalProduct);
			entityManager.flush();
			entityManager.clear();

			// 상품 정보 수정
			savedProduct.update("수정된 상품", 20000, "수정된 설명");

			// when
			Product updatedProduct = productRepository.save(savedProduct);

			// then
			assertThat(updatedProduct.getId()).isEqualTo(savedProduct.getId());
			assertThat(updatedProduct.getName()).isEqualTo("수정된 상품");
			assertThat(updatedProduct.getPrice()).isEqualTo(20000);
			assertThat(updatedProduct.getDetailContent()).isEqualTo("수정된 설명");

			// 데이터베이스에서 확인
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
			Product product = Product.create(
					"조회 테스트 상품",
					3L,
					4L,
					30000,
					200,
					Collections.emptyList(),
					"조회 테스트 설명"
			);
			Product savedProduct = productRepository.save(product);
			entityManager.flush();
			entityManager.clear();

			// when
			Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

			// then
			assertThat(foundProduct).isPresent();
			assertThat(foundProduct.get().getId()).isEqualTo(savedProduct.getId());
			assertThat(foundProduct.get().getName()).isEqualTo("조회 테스트 상품");
			assertThat(foundProduct.get().getCategoryId()).isEqualTo(3L);
			assertThat(foundProduct.get().getBrandId()).isEqualTo(4L);
			assertThat(foundProduct.get().getPrice()).isEqualTo(30000);
			assertThat(foundProduct.get().getStock()).isEqualTo(200);
			assertThat(foundProduct.get().getDetailContent()).isEqualTo("조회 테스트 설명");
		}

		@Test
		@DisplayName("존재하지 않는 상품을 ID로 조회하면 빈 Optional을 반환한다.")
		void 존재하지_않는_상품_ID_조회 () {
			// given
			Long nonExistentId = 999999L;

			// when
			Optional<Product> foundProduct = productRepository.findById(nonExistentId);

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
			Product product = Product.create(
					"중복 체크 상품",
					1L,
					2L,
					25000,
					75,
					Collections.emptyList(),
					"중복 체크 설명"
			);
			productRepository.save(product);
			entityManager.flush();

			// when
			boolean exists = productRepository.existsByName("중복 체크 상품");

			// then
			assertThat(exists).isTrue();
		}

		@Test
		@DisplayName("존재하지 않는 상품명인 경우 false를 반환한다.")
		void 존재하지_않는_상품명_확인 () {
			// when
			boolean exists = productRepository.existsByName("존재하지 않는 상품");

			// then
			assertThat(exists).isFalse();
		}
	}

	@Nested
	@DisplayName("상품명 중복 확인 (ID 제외)")
	class ExistsByNameAndIdNot {

		@Test
		@DisplayName("특정 ID를 제외하고 같은 이름의 상품이 존재하는 경우 true를 반환한다.")
		void 특정_ID_제외_중복_상품명_존재 () {
			// given
			Product product1 = Product.create(
					"동일 이름 상품",
					1L, 2L, 10000, 50,
					Collections.emptyList(), "설명1"
			);
			Product product2 = Product.create(
					"동일 이름 상품",
					1L, 2L, 20000, 100,
					Collections.emptyList(), "설명2"
			);

			Product savedProduct1 = productRepository.save(product1);
			productRepository.save(product2);
			entityManager.flush();

			// when - product1의 ID를 제외하고 "동일 이름 상품"이 존재하는지 확인
			boolean exists = productRepository.existsByNameAndIdNot("동일 이름 상품", savedProduct1.getId());

			// then - product2가 같은 이름을 가지고 있으므로 true
			assertThat(exists).isTrue();
		}

		@Test
		@DisplayName("특정 ID를 제외하고 같은 이름의 상품이 존재하지 않는 경우 false를 반환한다.")
		void 특정_ID_제외_중복_상품명_미존재 () {
			// given
			Product product = Product.create(
					"유일한 상품명",
					1L, 2L, 15000, 80,
					Collections.emptyList(), "유일한 설명"
			);
			Product savedProduct = productRepository.save(product);
			entityManager.flush();

			// when - 해당 상품의 ID를 제외하고 같은 이름이 존재하는지 확인
			boolean exists = productRepository.existsByNameAndIdNot("유일한 상품명", savedProduct.getId());

			// then - 다른 상품이 없으므로 false
			assertThat(exists).isFalse();
		}

		@Test
		@DisplayName("존재하지 않는 상품명인 경우 false를 반환한다.")
		void 존재하지_않는_상품명_ID_제외_확인 () {
			// when
			boolean exists = productRepository.existsByNameAndIdNot("전혀 없는 상품명", 123L);

			// then
			assertThat(exists).isFalse();
		}
	}
}