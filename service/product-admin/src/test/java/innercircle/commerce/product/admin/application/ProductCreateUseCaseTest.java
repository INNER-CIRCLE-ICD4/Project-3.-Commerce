package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.InvalidBrandException;
import innercircle.commerce.product.admin.application.exception.InvalidCategoryException;
import innercircle.commerce.product.admin.application.repository.BrandRepository;
import innercircle.commerce.product.admin.application.repository.CategoryRepository;
import innercircle.commerce.product.admin.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.entity.Product;
import innercircle.commerce.product.admin.fixtures.ProductCreateCommandFixtures;
import innercircle.commerce.product.admin.fixtures.ProductFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static innercircle.commerce.product.admin.fixtures.ProductFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 등록 UseCase 테스트")
class ProductCreateUseCaseTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private BrandRepository brandRepository;

	@Mock
	private CategoryRepository categoryRepository;

	private ProductCreateUseCase productCreateUseCase;

	@BeforeEach
	void setUp () {
		productCreateUseCase = new ProductCreateUseCase(productRepository, brandRepository, categoryRepository);
	}

	@Test
	@DisplayName("정상적인 상품 등록이 성공한다.")
	void 정상적인_상품_등록_성공 () {
		// given
		ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
		Product expectedProduct = ProductFixtures.createValidProduct();

		given(productRepository.existsByName(VALID_NAME)).willReturn(false);
		given(brandRepository.existsById(VALID_BRAND_ID)).willReturn(true);
		given(categoryRepository.existsById(VALID_CATEGORY_ID)).willReturn(true);
		given(productRepository.save(any(Product.class))).willReturn(expectedProduct);

		// when
		Product result = productCreateUseCase.create(command);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(VALID_NAME);
		verify(productRepository).save(any(Product.class));
	}

	@Test
	@DisplayName("옵션이 있는 상품 등록이 성공한다.")
	void 옵션이_있는_상품_등록_성공 () {
		// given
		ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommandWithOptions();
		Product expectedProduct = ProductFixtures.createValidProductWithOptions();

		given(productRepository.existsByName(VALID_NAME)).willReturn(false);
		given(brandRepository.existsById(VALID_BRAND_ID)).willReturn(true);
		given(categoryRepository.existsById(VALID_CATEGORY_ID)).willReturn(true);
		given(productRepository.save(any(Product.class))).willReturn(expectedProduct);

		// when
		Product result = productCreateUseCase.create(command);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(VALID_NAME);
		assertThat(result.getOptions()).hasSize(1);
		verify(productRepository).save(any(Product.class));
	}

	@Test
	@DisplayName("상품명이 중복되면 예외가 발생한다.")
	void 상품명_중복_시_예외_발생 () {
		// given
		ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();

		given(productRepository.existsByName(VALID_NAME)).willReturn(true);

		// when & then
		assertThatThrownBy(() -> productCreateUseCase.create(command))
				.isInstanceOf(DuplicateProductNameException.class)
				.hasMessageContaining(VALID_NAME);
	}

	@Test
	@DisplayName("유효하지 않은 브랜드 ID로 상품 등록 시 예외가 발생한다.")
	void 유효하지_않은_브랜드ID_시_예외_발생 () {
		// given
		ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();

		given(productRepository.existsByName(VALID_NAME)).willReturn(false);
		given(brandRepository.existsById(VALID_BRAND_ID)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> productCreateUseCase.create(command))
				.isInstanceOf(InvalidBrandException.class)
				.hasMessageContaining(VALID_BRAND_ID.toString());
	}

	@Test
	@DisplayName("유효하지 않은 카테고리 ID로 상품 등록 시 예외가 발생한다.")
	void 유효하지_않은_카테고리ID_시_예외_발생 () {
		// given
		ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();

		given(productRepository.existsByName(VALID_NAME)).willReturn(false);
		given(brandRepository.existsById(VALID_BRAND_ID)).willReturn(true);
		given(categoryRepository.existsById(VALID_CATEGORY_ID)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> productCreateUseCase.create(command))
				.isInstanceOf(InvalidCategoryException.class)
				.hasMessageContaining(VALID_CATEGORY_ID.toString());
	}
}
