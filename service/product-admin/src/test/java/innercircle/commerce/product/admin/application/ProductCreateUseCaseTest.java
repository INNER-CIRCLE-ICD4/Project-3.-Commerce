package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.dto.ProductImageInfo;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.InvalidBrandException;
import innercircle.commerce.product.admin.application.exception.InvalidCategoryException;
import innercircle.commerce.product.admin.application.exception.NotFoundTempImageException;
import innercircle.commerce.product.admin.fixtures.ProductCreateCommandFixtures;
import innercircle.commerce.product.core.application.repository.BrandRepository;
import innercircle.commerce.product.core.application.repository.CategoryRepository;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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

	@InjectMocks
	private ProductCreateUseCase productCreateUseCase;

	@Nested
	@DisplayName("상품 등록")
	class Create {

		@Test
		@DisplayName("유효한 명령어로 상품을 정상적으로 등록할 수 있다.")
		void 상품_등록_성공 () {
			// given
			ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
			Product savedProduct = mock(Product.class);

			given(productRepository.existsByName(any(String.class))).willReturn(false);
			given(brandRepository.existsById(any(Long.class))).willReturn(true);
			given(categoryRepository.existsById(any(Long.class))).willReturn(true);
			given(productRepository.save(any(Product.class))).willReturn(savedProduct);
			given(savedProduct.getId()).willReturn(1L);
			
			willDoNothing().given(savedProduct).addImages(any());

			// S3 이미지 이동 성공 모킹
			String movedUrl = "https://s3.amazonaws.com/bucket/commerce/products/1/1.jpg";
			given(s3ImageStore.move(any(String.class), any(String.class)))
					.willReturn(Optional.of(movedUrl));

			// when
			Product result = productCreateUseCase.create(command);

			// then
			assertThat(result).isNotNull();
			verify(productRepository).existsByName(command.name());
			verify(brandRepository).existsById(command.brandId());
			verify(categoryRepository).existsById(command.leafCategoryId());
			verify(productRepository, times(2)).save(any(Product.class));
			
			List<ProductImageInfo> imageInfos = command.imageInfos();
			verify(s3ImageStore, times(imageInfos.size())).move(any(String.class), any(String.class));
		}

		@Test
		@DisplayName("상품명이 중복된 경우 DuplicateProductNameException이 발생한다.")
		void 상품명_중복_예외 () {
			// given
			ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
			given(productRepository.existsByName(command.name())).willReturn(true);

			// when & then
			assertThatThrownBy(() -> productCreateUseCase.create(command))
					.isInstanceOf(DuplicateProductNameException.class);
		}

		@Test
		@DisplayName("존재하지 않는 브랜드 ID인 경우 InvalidBrandException이 발생한다.")
		void 브랜드_존재하지_않음_예외 () {
			// given
			ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
			given(productRepository.existsByName(any(String.class))).willReturn(false);
			given(brandRepository.existsById(command.brandId())).willReturn(false);

			// when & then
			assertThatThrownBy(() -> productCreateUseCase.create(command))
					.isInstanceOf(InvalidBrandException.class);
		}

		@Test
		@DisplayName("존재하지 않는 카테고리 ID인 경우 InvalidCategoryException이 발생한다.")
		void 카테고리_존재하지_않음_예외 () {
			// given
			ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
			given(productRepository.existsByName(any(String.class))).willReturn(false);
			given(brandRepository.existsById(any(Long.class))).willReturn(true);
			given(categoryRepository.existsById(command.leafCategoryId())).willReturn(false);

			// when & then
			assertThatThrownBy(() -> productCreateUseCase.create(command))
					.isInstanceOf(InvalidCategoryException.class);
		}

		@Test
		@DisplayName("임시 이미지를 찾을 수 없는 경우 NotFoundTempImageException이 발생한다.")
		void 임시_이미지_찾을_수_없음_예외 () {
			// given
			ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
			Product savedProduct = mock(Product.class);

			given(productRepository.existsByName(any(String.class))).willReturn(false);
			given(brandRepository.existsById(any(Long.class))).willReturn(true);
			given(categoryRepository.existsById(any(Long.class))).willReturn(true);
			given(productRepository.save(any(Product.class))).willReturn(savedProduct);
			given(savedProduct.getId()).willReturn(1L);
			given(s3ImageStore.move(any(String.class), any(String.class)))
					.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> productCreateUseCase.create(command))
					.isInstanceOf(NotFoundTempImageException.class);
		}
	}
}