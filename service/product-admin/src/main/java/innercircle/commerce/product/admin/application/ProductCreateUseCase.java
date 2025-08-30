package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.dto.ProductImageInfo;
import innercircle.commerce.product.admin.application.exception.NotFoundTempImageException;
import innercircle.commerce.product.admin.application.validator.ProductCreateCommandValidator;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductImage;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import innercircle.commerce.product.infra.s3.S3UrlHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 상품 등록 UseCase
 */
@Service
@RequiredArgsConstructor
public class ProductCreateUseCase {
	private final ProductRepository productRepository;
	private final S3ImageStore s3ImageStore;
	private final ProductCreateCommandValidator validator;
	private final S3UrlHelper s3UrlHelper;


	/**
	 * 이미지와 함께 상품을 등록합니다.
	 *
	 * @param command 상품 등록 명령 (이미지 포함)
	 * @return 등록된 상품
	 * @throws NotFoundTempImageException 임시 이미지를 찾을 수 없는 경우
	 */
	@Transactional
	public Product create (ProductCreateCommand command) {
		validator.validate(command);

		Product product = command.toDomain();

		List<ProductImage> productImages = moveImagesToProduct(product.getId(), command.imageInfos());

		product.addImages(productImages);
		return productRepository.save(product);
	}


	/**
	 * 임시 이미지들을 상품 이미지로 이동합니다.
	 *
	 * @param productId  상품 ID
	 * @param imageInfos 임시 이미지 정보 목록
	 * @return 이동된 상품 이미지 목록
	 * @throws NotFoundTempImageException 임시 이미지를 찾을 수 없는 경우
	 */
	private List<ProductImage> moveImagesToProduct (Long productId, List<ProductImageInfo> imageInfos) {
		List<ProductImage> productImages = new ArrayList<>();

		for (ProductImageInfo imageInfo : imageInfos) {
			String tempKey = s3UrlHelper.extractKeyFromUrlComplex(imageInfo.url());
			String extension = s3UrlHelper.extractExtensionFromUrlOrName(imageInfo.url(), imageInfo.originalName());
			String productKey = s3UrlHelper.buildProductImageKey(productId, imageInfo.id(), extension);

			var movedUrl = s3ImageStore.move(tempKey, productKey);
			if (movedUrl.isEmpty()) {
				throw new NotFoundTempImageException(imageInfo.id().toString());
			}

			ProductImage productImage = ProductImage.create(
					productId,
					movedUrl.get(),
					imageInfo.originalName(),
					imageInfo.sortOrder()
			);

			productImages.add(productImage);
		}

		return productImages;
	}


}
