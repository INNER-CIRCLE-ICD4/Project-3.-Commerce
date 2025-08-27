package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductSaleTypeChangeCommand;
import innercircle.commerce.product.admin.application.dto.ProductStatusChangeCommand;
import innercircle.commerce.product.admin.application.dto.ProductUpdateCommand;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductImage;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import innercircle.commerce.product.admin.fixtures.ProductFixtures;
import innercircle.commerce.product.admin.fixtures.ProductImageFixtures;
import innercircle.commerce.product.admin.fixtures.ProductUpdateCommandFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static innercircle.commerce.product.admin.fixtures.ProductUpdateCommandFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 수정 UseCase 테스트")
class ProductUpdateUseCaseTest {

	@Mock
	private ProductRepository productRepository;
	
	@Mock
	private S3ImageStore s3ImageStore;

	private ProductUpdateUseCase productUpdateUseCase;

	@BeforeEach
	void setUp () {
		productUpdateUseCase = new ProductUpdateUseCase(productRepository, s3ImageStore);
	}

	@Test
	@DisplayName("상품 기본 정보 수정이 성공한다.")
	void 상품_기본정보_수정_성공 () {
		// given
		ProductUpdateCommand command = ProductUpdateCommandFixtures.createValidUpdateCommand();
		Product existingProduct = ProductFixtures.createValidProduct();
		Product updatedProduct = ProductFixtures.createValidProduct();

		given(productRepository.findById(EXISTING_PRODUCT_ID)).willReturn(Optional.of(existingProduct));
		given(productRepository.existsByNameAndIdNot(UPDATED_NAME, EXISTING_PRODUCT_ID)).willReturn(false);
		given(productRepository.save(any(Product.class))).willReturn(updatedProduct);

		// when
		Product result = productUpdateUseCase.updateProduct(command);

		// then
		assertThat(result).isNotNull();
		verify(productRepository).save(any(Product.class));
	}

	@Test
	@DisplayName("존재하지 않는 상품 수정 시 예외가 발생한다.")
	void 존재하지_않는_상품_수정_시_예외_발생 () {
		// given
		ProductUpdateCommand command = ProductUpdateCommandFixtures.createValidUpdateCommand();

		given(productRepository.findById(EXISTING_PRODUCT_ID)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> productUpdateUseCase.updateProduct(command))
				.isInstanceOf(ProductNotFoundException.class)
				.hasMessageContaining(EXISTING_PRODUCT_ID.toString());
	}

	@Test
	@DisplayName("수정 시 상품명이 중복되면 예외가 발생한다.")
	void 수정_시_상품명_중복되면_예외_발생 () {
		// given
		ProductUpdateCommand command = ProductUpdateCommandFixtures.createValidUpdateCommand();
		Product existingProduct = ProductFixtures.createValidProduct();

		given(productRepository.findById(EXISTING_PRODUCT_ID)).willReturn(Optional.of(existingProduct));
		given(productRepository.existsByNameAndIdNot(UPDATED_NAME, EXISTING_PRODUCT_ID)).willReturn(true);

		// when & then
		assertThatThrownBy(() -> productUpdateUseCase.updateProduct(command))
				.isInstanceOf(DuplicateProductNameException.class)
				.hasMessageContaining(UPDATED_NAME);
	}

	@Test
	@DisplayName("이미지 삭제와 함께 상품 수정이 성공한다.")
	void 이미지_삭제와_함께_상품_수정_성공 () {
		// given
		List<String> imagesToDelete = List.of("http://example.com/sub-image.jpg");
		ProductUpdateCommand command = new ProductUpdateCommand(
				EXISTING_PRODUCT_ID,
				UPDATED_NAME,
				UPDATED_PRICE,
				UPDATED_DETAIL_CONTENT,
				imagesToDelete,
				null
		);

		Product existingProduct = ProductFixtures.createValidProductWithImages();
		Product updatedProduct = ProductFixtures.createValidProduct();

		given(productRepository.findById(EXISTING_PRODUCT_ID)).willReturn(Optional.of(existingProduct));
		given(productRepository.existsByNameAndIdNot(UPDATED_NAME, EXISTING_PRODUCT_ID)).willReturn(false);
		given(productRepository.save(any(Product.class))).willReturn(updatedProduct);

		// when
		Product result = productUpdateUseCase.updateProduct(command);

		// then
		assertThat(result).isNotNull();
		verify(s3ImageStore).delete(anyString());
		verify(productRepository).save(any(Product.class));
	}

	@Test
	@DisplayName("이미지 추가와 함께 상품 수정이 성공한다.")
	void 이미지_추가와_함께_상품_수정_성공 () {
		// given
		List<ProductImage> imagesToAdd = List.of(
				ProductImageFixtures.createValidProductImage(EXISTING_PRODUCT_ID)
		);
		ProductUpdateCommand command = new ProductUpdateCommand(
				EXISTING_PRODUCT_ID,
				UPDATED_NAME,
				UPDATED_PRICE,
				UPDATED_DETAIL_CONTENT,
				null,
				imagesToAdd
		);

		Product existingProduct = ProductFixtures.createValidProductWithImages();
		Product updatedProduct = ProductFixtures.createValidProduct();

		given(productRepository.findById(EXISTING_PRODUCT_ID)).willReturn(Optional.of(existingProduct));
		given(productRepository.existsByNameAndIdNot(UPDATED_NAME, EXISTING_PRODUCT_ID)).willReturn(false);
		given(s3ImageStore.move(anyString(), anyString())).willReturn(Optional.of("https://example.com/final/image.jpg"));
		given(productRepository.save(any(Product.class))).willReturn(updatedProduct);

		// when
		Product result = productUpdateUseCase.updateProduct(command);

		// then
		assertThat(result).isNotNull();
		verify(s3ImageStore).move(anyString(), anyString());
		verify(productRepository).save(any(Product.class));
	}
}
