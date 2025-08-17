package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.dto.ProductImageInfo;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.InvalidBrandException;
import innercircle.commerce.product.admin.application.exception.InvalidCategoryException;
import innercircle.commerce.product.admin.application.exception.NotFoundTempImageException;
import innercircle.commerce.product.core.application.repository.BrandRepository;
import innercircle.commerce.product.core.application.repository.CategoryRepository;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductImage;
import innercircle.commerce.product.infra.s3.S3ImageStore;
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
	private final BrandRepository brandRepository;
	private final CategoryRepository categoryRepository;
	private final S3ImageStore s3ImageStore;


	/**
	 * 이미지와 함께 상품을 등록합니다.
	 *
	 * @param command 상품 등록 명령 (이미지 포함)
	 * @return 등록된 상품
	 * @throws DuplicateProductNameException 상품명이 중복된 경우
	 * @throws InvalidBrandException         유효하지 않은 브랜드 ID인 경우
	 * @throws InvalidCategoryException      유효하지 않은 카테고리 ID인 경우
	 * @throws NotFoundTempImageException    임시 이미지를 찾을 수 없는 경우
	 */
	@Transactional
	public Product create (ProductCreateCommand command) {
		validateProductNameDuplicate(command.name());
		validateBrandExists(command.brandId());
		validateCategoryExists(command.leafCategoryId());

		Product product = command.toDomain();
		Product savedProduct = productRepository.save(product);

		List<ProductImage> productImages = moveImagesToProduct(savedProduct.getId(), command.imageInfos());

		savedProduct.addImages(productImages);

		return productRepository.save(savedProduct);
	}

	/**
	 * 상품명 중복 여부를 검증합니다.
	 *
	 * @param productName 상품명
	 * @throws DuplicateProductNameException 중복된 경우
	 */
	private void validateProductNameDuplicate (String productName) {
		if (productRepository.existsByName(productName)) {
			throw new DuplicateProductNameException(productName);
		}
	}

	/**
	 * 브랜드 존재 여부를 검증합니다.
	 *
	 * @param brandId 브랜드 ID
	 * @throws InvalidBrandException 존재하지 않는 경우
	 */
	private void validateBrandExists (Long brandId) {
		if (!brandRepository.existsById(brandId)) {
			throw new InvalidBrandException(brandId);
		}
	}

	/**
	 * 카테고리 존재 여부를 검증합니다.
	 *
	 * @param categoryId 카테고리 ID
	 * @throws InvalidCategoryException 존재하지 않는 경우
	 */
	private void validateCategoryExists (Long categoryId) {
		if (!categoryRepository.existsById(categoryId)) {
			throw new InvalidCategoryException(categoryId);
		}
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

		for (int i = 0; i < imageInfos.size(); i++) {
			ProductImageInfo imageInfo = imageInfos.get(i);

			// URL에서 임시 키 추출 (URL 형식: https://bucket.s3.region.amazonaws.com/key)
			String tempKey = extractS3KeyFromUrl(imageInfo.url());

			// 확장자 추출 (URL 또는 originalName에서)
			String extension = extractExtensionFromUrlOrName(imageInfo.url(), imageInfo.originalName());

			String productKey = buildProductPath(productId, (long) (i + 1), extension);

			var movedUrl = s3ImageStore.move(tempKey, productKey);
			if (movedUrl.isEmpty()) {
				throw new NotFoundTempImageException(imageInfo.id().toString());
			}

			ProductImage productImage = ProductImage.create(
					productId,
					movedUrl.get(),
					imageInfo.originalName(),
					imageInfo.isMain(),
					imageInfo.sortOrder()
			);

			productImages.add(productImage);
		}

		return productImages;
	}

	/**
	 * 상품 이미지 경로 생성
	 */
	private String buildProductPath (Long productId, Long imageId, String extension) {
		return String.format("commerce/products/%d/%d.%s", productId, imageId, extension);
	}

	/**
	 * URL에서 S3 키를 추출합니다.
	 *
	 * @param url S3 URL
	 * @return S3 키
	 */
	private String extractS3KeyFromUrl (String url) {
		// URL 형식: https://bucket.s3.region.amazonaws.com/key
		// 또는: https://bucket.s3.amazonaws.com/key
		String[] parts = url.split("/");
		StringBuilder keyBuilder = new StringBuilder();
		boolean foundAmazonaws = false;

		for (String part : parts) {
			if (foundAmazonaws) {
				if (!keyBuilder.isEmpty()) {
					keyBuilder.append("/");
				}
				keyBuilder.append(part);
			} else if (part.contains("amazonaws.com")) {
				foundAmazonaws = true;
			}
		}

		return keyBuilder.toString();
	}

	/**
	 * URL 또는 파일명에서 확장자를 추출합니다.
	 *
	 * @param url          S3 URL
	 * @param originalName 원본 파일명
	 * @return 확장자 (점 제외)
	 */
	private String extractExtensionFromUrlOrName (String url, String originalName) {
		// URL에서 확장자 추출 시도
		if (url != null && url.contains(".")) {
			String[] urlParts = url.split("\\.");
			String extension = urlParts[urlParts.length - 1];
			// 쿼리 파라미터가 있으면 제거
			if (extension.contains("?")) {
				extension = extension.split("\\?")[0];
			}
			return extension.toLowerCase();
		}

		// 원본 파일명에서 확장자 추출
		if (originalName != null && originalName.contains(".")) {
			String[] nameParts = originalName.split("\\.");
			return nameParts[nameParts.length - 1].toLowerCase();
		}

		// 기본값
		return "jpg";
	}

}
