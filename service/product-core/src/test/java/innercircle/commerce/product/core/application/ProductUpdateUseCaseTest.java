package innercircle.commerce.product.core.application;

import innercircle.commerce.product.core.application.dto.ProductImageUpdateCommand;
import innercircle.commerce.product.core.application.dto.ProductSaleTypeChangeCommand;
import innercircle.commerce.product.core.application.dto.ProductStatusChangeCommand;
import innercircle.commerce.product.core.application.dto.ProductUpdateCommand;
import innercircle.commerce.product.core.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.core.application.exception.ProductNotFoundException;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.entity.Product;
import innercircle.commerce.product.core.domain.entity.ProductStatus;
import innercircle.commerce.product.core.domain.entity.SaleType;
import innercircle.commerce.product.core.fixtures.ProductFixtures;
import innercircle.commerce.product.core.fixtures.ProductUpdateCommandFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static innercircle.commerce.product.core.fixtures.ProductUpdateCommandFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 수정 UseCase 테스트")
class ProductUpdateUseCaseTest {

	@Mock
	private ProductRepository productRepository;

	private ProductUpdateUseCase productUpdateUseCase;

	@BeforeEach
	void setUp () {
		productUpdateUseCase = new ProductUpdateUseCase(productRepository);
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
		Product result = productUpdateUseCase.updateBasicInfo(command);

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
		assertThatThrownBy(() -> productUpdateUseCase.updateBasicInfo(command))
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
		assertThatThrownBy(() -> productUpdateUseCase.updateBasicInfo(command))
				.isInstanceOf(DuplicateProductNameException.class)
				.hasMessageContaining(UPDATED_NAME);
	}

	@Test
	@DisplayName("상품 상태 변경이 성공한다.")
	void 상품_상태_변경_성공 () {
		// given
		ProductStatusChangeCommand command = ProductUpdateCommandFixtures.createStatusChangeCommand();
		Product existingProduct = ProductFixtures.createValidProduct();

		given(productRepository.findById(EXISTING_PRODUCT_ID)).willReturn(Optional.of(existingProduct));
		given(productRepository.save(any(Product.class))).willReturn(existingProduct);

		// when
		Product result = productUpdateUseCase.changeStatus(command);

		// then
		assertThat(result).isNotNull();
		verify(productRepository).save(any(Product.class));
	}

	@Test
	@DisplayName("존재하지 않는 상품의 상태 변경 시 예외가 발생한다.")
	void 존재하지_않는_상품_상태_변경_시_예외_발생 () {
		// given
		ProductStatusChangeCommand command = ProductUpdateCommandFixtures.createStatusChangeCommand();

		given(productRepository.findById(EXISTING_PRODUCT_ID)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> productUpdateUseCase.changeStatus(command))
				.isInstanceOf(ProductNotFoundException.class)
				.hasMessageContaining(EXISTING_PRODUCT_ID.toString());
	}

	@Test
	@DisplayName("상품 판매 유형 변경이 성공한다.")
	void 상품_판매유형_변경_성공 () {
		// given
		ProductSaleTypeChangeCommand command = ProductUpdateCommandFixtures.createSaleTypeChangeCommand();
		Product existingProduct = ProductFixtures.createValidProduct();

		given(productRepository.findById(EXISTING_PRODUCT_ID)).willReturn(Optional.of(existingProduct));
		given(productRepository.save(any(Product.class))).willReturn(existingProduct);

		// when
		Product result = productUpdateUseCase.changeSaleType(command);

		// then
		assertThat(result).isNotNull();
		verify(productRepository).save(any(Product.class));
	}

	@Test
	@DisplayName("존재하지 않는 상품의 판매 유형 변경 시 예외가 발생한다.")
	void 존재하지_않는_상품_판매유형_변경_시_예외_발생 () {
		// given
		ProductSaleTypeChangeCommand command = ProductUpdateCommandFixtures.createSaleTypeChangeCommand();

		given(productRepository.findById(EXISTING_PRODUCT_ID)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> productUpdateUseCase.changeSaleType(command))
				.isInstanceOf(ProductNotFoundException.class)
				.hasMessageContaining(EXISTING_PRODUCT_ID.toString());
	}

	@Test
	@DisplayName("상품 이미지 변경이 성공한다.")
	void 상품_이미지_변경_성공 () {
		// given
		ProductImageUpdateCommand command = ProductUpdateCommandFixtures.createImageUpdateCommand();
		Product existingProduct = ProductFixtures.createValidProduct();

		given(productRepository.findById(EXISTING_PRODUCT_ID)).willReturn(Optional.of(existingProduct));
		given(productRepository.save(any(Product.class))).willReturn(existingProduct);

		// when
		Product result = productUpdateUseCase.updateImages(command);

		// then
		assertThat(result).isNotNull();
		verify(productRepository).save(any(Product.class));
	}

	@Test
	@DisplayName("존재하지 않는 상품의 이미지 변경 시 예외가 발생한다.")
	void 존재하지_않는_상품_이미지_변경_시_예외_발생 () {
		// given
		ProductImageUpdateCommand command = ProductUpdateCommandFixtures.createImageUpdateCommand();

		given(productRepository.findById(EXISTING_PRODUCT_ID)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> productUpdateUseCase.updateImages(command))
				.isInstanceOf(ProductNotFoundException.class)
				.hasMessageContaining(EXISTING_PRODUCT_ID.toString());
	}
}
