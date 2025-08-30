package innercircle.commerce.product.admin.application.validator;

import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.InvalidBrandException;
import innercircle.commerce.product.admin.application.exception.InvalidCategoryException;
import innercircle.commerce.product.admin.fixtures.ProductCreateCommandFixtures;
import innercircle.commerce.product.core.application.repository.BrandRepository;
import innercircle.commerce.product.core.application.repository.CategoryRepository;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 등록 Command Validator 테스트")
class ProductCreateCommandValidatorTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private BrandRepository brandRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@InjectMocks
	private ProductCreateCommandValidator validator;

	@Nested
	@DisplayName("전체 검증")
	class Validate {

		@Test
		@DisplayName("유효한 명령어인 경우 검증이 성공한다.")
		void 검증_성공() {
			// given
			ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
			given(productRepository.existsByName(any(String.class))).willReturn(false);
			given(brandRepository.existsById(any(Long.class))).willReturn(true);
			given(categoryRepository.existsById(any(Long.class))).willReturn(true);

			// when & then
			assertThatNoException().isThrownBy(() -> validator.validate(command));
		}

		@Test
		@DisplayName("상품명이 중복된 경우 DuplicateProductNameException이 발생한다.")
		void 상품명_중복_예외() {
			// given
			ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
			given(productRepository.existsByName(eq(command.name()))).willReturn(true);

			// when & then
			assertThatThrownBy(() -> validator.validate(command))
					.isInstanceOf(DuplicateProductNameException.class);
		}

		@Test
		@DisplayName("존재하지 않는 브랜드 ID인 경우 InvalidBrandException이 발생한다.")
		void 브랜드_존재하지_않음_예외() {
			// given
			ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
			given(productRepository.existsByName(any(String.class))).willReturn(false);
			given(brandRepository.existsById(eq(command.brandId()))).willReturn(false);

			// when & then
			assertThatThrownBy(() -> validator.validate(command))
					.isInstanceOf(InvalidBrandException.class);
		}

		@Test
		@DisplayName("존재하지 않는 카테고리 ID인 경우 InvalidCategoryException이 발생한다.")
		void 카테고리_존재하지_않음_예외() {
			// given
			ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
			given(productRepository.existsByName(any(String.class))).willReturn(false);
			given(brandRepository.existsById(any(Long.class))).willReturn(true);
			given(categoryRepository.existsById(eq(command.leafCategoryId()))).willReturn(false);

			// when & then
			assertThatThrownBy(() -> validator.validate(command))
					.isInstanceOf(InvalidCategoryException.class);
		}
	}

	@Nested
	@DisplayName("상품명 중복 검증")
	class ValidateProductNameDuplicate {

		@Test
		@DisplayName("중복되지 않은 상품명인 경우 검증이 성공한다.")
		void 상품명_중복_없음_검증_성공() {
			// given
			String productName = "테스트 상품";
			given(productRepository.existsByName(eq(productName))).willReturn(false);

			// when & then
			assertThatNoException().isThrownBy(() -> validator.validateProductNameDuplicate(productName));
		}

		@Test
		@DisplayName("중복된 상품명인 경우 DuplicateProductNameException이 발생한다.")
		void 상품명_중복_예외() {
			// given
			String productName = "중복 상품";
			given(productRepository.existsByName(eq(productName))).willReturn(true);

			// when & then
			assertThatThrownBy(() -> validator.validateProductNameDuplicate(productName))
					.isInstanceOf(DuplicateProductNameException.class);
		}
	}

	@Nested
	@DisplayName("브랜드 존재 검증")
	class ValidateBrandExists {

		@Test
		@DisplayName("존재하는 브랜드 ID인 경우 검증이 성공한다.")
		void 브랜드_존재_검증_성공() {
			// given
			Long brandId = 1L;
			given(brandRepository.existsById(eq(brandId))).willReturn(true);

			// when & then
			assertThatNoException().isThrownBy(() -> validator.validateBrandExists(brandId));
		}

		@Test
		@DisplayName("존재하지 않는 브랜드 ID인 경우 InvalidBrandException이 발생한다.")
		void 브랜드_존재하지_않음_예외() {
			// given
			Long brandId = 999L;
			given(brandRepository.existsById(eq(brandId))).willReturn(false);

			// when & then
			assertThatThrownBy(() -> validator.validateBrandExists(brandId))
					.isInstanceOf(InvalidBrandException.class);
		}
	}

	@Nested
	@DisplayName("카테고리 존재 검증")
	class ValidateCategoryExists {

		@Test
		@DisplayName("존재하는 카테고리 ID인 경우 검증이 성공한다.")
		void 카테고리_존재_검증_성공() {
			// given
			Long categoryId = 1L;
			given(categoryRepository.existsById(eq(categoryId))).willReturn(true);

			// when & then
			assertThatNoException().isThrownBy(() -> validator.validateCategoryExists(categoryId));
		}

		@Test
		@DisplayName("존재하지 않는 카테고리 ID인 경우 InvalidCategoryException이 발생한다.")
		void 카테고리_존재하지_않음_예외() {
			// given
			Long categoryId = 999L;
			given(categoryRepository.existsById(eq(categoryId))).willReturn(false);

			// when & then
			assertThatThrownBy(() -> validator.validateCategoryExists(categoryId))
					.isInstanceOf(InvalidCategoryException.class);
		}
	}
}