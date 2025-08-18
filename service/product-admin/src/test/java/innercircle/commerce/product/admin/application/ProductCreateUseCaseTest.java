package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.dto.ProductImageInfo;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.NotFoundTempImageException;
import innercircle.commerce.product.admin.application.validator.ProductCreateCommandValidator;
import innercircle.commerce.product.admin.fixtures.ProductCreateCommandFixtures;
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
	private S3ImageStore s3ImageStore;

	@Mock
	private ProductCreateCommandValidator validator;

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
			Product product = command.toDomain();

			willDoNothing().given(validator).validate(eq(command));
			given(productRepository.save(any(Product.class))).willReturn(product);

			// S3 이미지 이동 성공 모킹
			String movedUrl = "https://s3.amazonaws.com/bucket/commerce/products/1/1.jpg";
			given(s3ImageStore.move(any(String.class), any(String.class)))
					.willReturn(Optional.of(movedUrl));

			// when
			Product result = productCreateUseCase.create(command);

			// then
			assertThat(result).isNotNull();
			verify(validator).validate(eq(command));
			verify(productRepository, times(1)).save(any(Product.class));

			List<ProductImageInfo> imageInfos = command.imageInfos();
			verify(s3ImageStore, times(imageInfos.size())).move(any(String.class), any(String.class));
		}

		@Test
		@DisplayName("검증 실패 시 validator에서 발생한 예외가 전파된다.")
		void 검증_실패_예외_전파 () {
			// given
			ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();
			willThrow(new DuplicateProductNameException(command.name()))
					.given(validator).validate(eq(command));

			// when & then
			assertThatThrownBy(() -> productCreateUseCase.create(command))
					.isInstanceOf(DuplicateProductNameException.class);

			// validator만 호출되고 다른 로직은 실행되지 않음을 확인
			verify(validator).validate(eq(command));
			verify(productRepository, never()).save(any(Product.class));
			verify(s3ImageStore, never()).move(any(String.class), any(String.class));
		}

		@Test
		@DisplayName("임시 이미지를 찾을 수 없는 경우 NotFoundTempImageException이 발생한다.")
		void 임시_이미지_찾을_수_없음_예외 () {
			// given
			ProductCreateCommand command = ProductCreateCommandFixtures.createValidCommand();

			willDoNothing().given(validator).validate(eq(command));
			given(s3ImageStore.move(any(String.class), any(String.class)))
					.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> productCreateUseCase.create(command))
					.isInstanceOf(NotFoundTempImageException.class);
		}
	}
}