package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.InvalidBrandException;
import innercircle.commerce.product.admin.application.exception.InvalidCategoryException;
import innercircle.commerce.product.admin.application.repository.BrandRepository;
import innercircle.commerce.product.admin.application.repository.CategoryRepository;
import innercircle.commerce.product.admin.application.repository.ProductRepository;
import innercircle.commerce.product.admin.application.validator.ImageValidator;
import innercircle.commerce.product.core.domain.entity.Product;
import innercircle.commerce.product.admin.fixtures.ProductCreateCommandFixtures;
import innercircle.commerce.product.admin.fixtures.ProductFixtures;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static innercircle.commerce.product.admin.fixtures.ProductFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 등록 UseCase 테스트")
class ProductCreateUseCaseTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private BrandRepository brandRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private S3ImageStore s3ImageStore;

	@Mock
	private ImageValidator imageValidator;

	private ProductCreateUseCase productCreateUseCase;

	@BeforeEach
	void setUp () {
		productCreateUseCase = new ProductCreateUseCase(
				productRepository,
				brandRepository,
				categoryRepository,
				s3ImageStore,
				imageValidator
		);
	}

	@Test
	@DisplayName("정상적인 상품 등록이 성공한다.")
	void 정상적인_상품_등록_성공 () throws IOException {
		// given
		ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
		Product expectedProduct = ProductFixtures.createValidProduct();

		// 기본 검증 Mock
		given(productRepository.existsByName(VALID_NAME)).willReturn(false);
		given(brandRepository.existsById(VALID_BRAND_ID)).willReturn(true);
		given(categoryRepository.existsById(VALID_CATEGORY_ID)).willReturn(true);
		given(productRepository.save(any(Product.class))).willReturn(expectedProduct);

		// S3ImageStore Mock 설정
		given(s3ImageStore.upload(any(), anyString()))
				.willReturn(new S3ImageStore.UploadedImageInfo("test.jpg", "https://s3.com/temp/1", "temp/1", 100L, "image/jpeg"));
		given(s3ImageStore.move(anyString(), anyString()))
				.willReturn(Optional.of("https://s3.com/products/1/1.jpg"));

		// when
		Product result = productCreateUseCase.create(command);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(VALID_NAME);
		verify(imageValidator, times(2)).validate(any());
		verify(s3ImageStore, times(2)).upload(any(), anyString());
		verify(s3ImageStore, times(2)).move(anyString(), anyString());
		verify(productRepository, times(2)).save(any(Product.class)); // 첫 번째 저장 + 이미지 업데이트 후 저장
	}

	@Test
	@DisplayName("옵션이 있는 상품 등록이 성공한다.")
	void 옵션이_있는_상품_등록_성공 () throws IOException {
		// given
		ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommandWithOptions();
		Product expectedProduct = ProductFixtures.createValidProductWithOptions();

		// 기본 검증 Mock
		given(productRepository.existsByName(VALID_NAME)).willReturn(false);
		given(brandRepository.existsById(VALID_BRAND_ID)).willReturn(true);
		given(categoryRepository.existsById(VALID_CATEGORY_ID)).willReturn(true);
		given(productRepository.save(any(Product.class))).willReturn(expectedProduct);

		// S3ImageStore Mock 설정
		given(s3ImageStore.upload(any(), anyString()))
				.willReturn(new S3ImageStore.UploadedImageInfo("test.jpg", "https://s3.com/temp/1", "temp/1", 100L, "image/jpeg"));
		given(s3ImageStore.move(anyString(), anyString()))
				.willReturn(Optional.of("https://s3.com/products/1/1.jpg"));

		// when
		Product result = productCreateUseCase.create(command);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(VALID_NAME);
		assertThat(result.getOptions()).hasSize(1);
		verify(imageValidator, times(2)).validate(any());
		verify(productRepository, times(2)).save(any(Product.class)); // 첫 번째 저장 + 이미지 업데이트 후 저장
	}

	@Test
	@DisplayName("상품명이 중복되면 예외가 발생한다.")
	void 상품명_중복_시_예외_발생 () {
		// given
		ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommandForExceptionTest();

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
		ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommandForExceptionTest();

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
		ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommandForExceptionTest();

		given(productRepository.existsByName(VALID_NAME)).willReturn(false);
		given(brandRepository.existsById(VALID_BRAND_ID)).willReturn(true);
		given(categoryRepository.existsById(VALID_CATEGORY_ID)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> productCreateUseCase.create(command))
				.isInstanceOf(InvalidCategoryException.class)
				.hasMessageContaining(VALID_CATEGORY_ID.toString());
	}

}
