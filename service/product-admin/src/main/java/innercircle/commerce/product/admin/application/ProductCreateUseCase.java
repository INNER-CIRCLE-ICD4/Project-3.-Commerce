package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.InvalidBrandException;
import innercircle.commerce.product.admin.application.exception.InvalidCategoryException;
import innercircle.commerce.product.core.application.repository.BrandRepository;
import innercircle.commerce.product.core.application.repository.CategoryRepository;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.entity.Product;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
	 */
	public Product create (ProductCreateCommand command) {
		// 1. 기본 검증
		validateProductNameDuplicate(command.name());
		validateBrandExists(command.brandId());
		validateCategoryExists(command.leafCategoryId());

		return null;
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
	 */
	//	private List<ProductImage> moveImagesToProduct (Long productId, List<ProductImageInfo> imageInfos) {
	//		List<ProductImage> productImages = new ArrayList<>();
	//
	//		for (int i = 0; i < imageInfos.size(); i++) {
	//			ProductImageInfo imageInfo = imageInfos.get(i);
	//
	//			// 임시 키에서 확장자를 추출하기 위해 S3에서 조회하거나, 기본값 사용
	//			String extension = "jpg"; // 실제로는 S3에서 메타데이터를 조회하여 확장자를 찾아야 함
	//			String tempKey = buildTempPath(imageInfo.tempId(), extension);
	//			String productKey = buildProductPath(productId, (long) (i + 1), extension);
	//
	//			var movedUrl = s3ImageStore.move(tempKey, productKey);
	//			if (movedUrl.isEmpty()) {
	//				throw new NotFoundTempImageException(imageInfo.tempId());
	//			}
	//
	//			ProductImage productImage = ProductImage.builder()
	//													.id((long) (i + 1))
	//													.productId(productId)
	//													.url(movedUrl.get())
	//													.isMain(imageInfo.isMain())
	//													.sortOrder(imageInfo.sortOrder())
	//													.build();
	//
	//			productImages.add(productImage);
	//		}
	//
	//		return productImages;
	//	}

	/**
	 * 임시 경로 생성
	 */
	private String buildTempPath (String tempId, String extension) {
		return String.format("commerce/temp/images/%s/original.%s", tempId, extension);
	}

	/**
	 * 상품 이미지 경로 생성
	 */
	private String buildProductPath (Long productId, Long imageId, String extension) {
		return String.format("commerce/products/%d/%d.%s", productId, imageId, extension);
	}

	/**
	 * 임시 키들 생성
	 */
	private List<String> buildTempKeys (List<String> tempIds) {
		return tempIds.stream()
					  .map(tempId -> buildTempPath(tempId, "jpg")) // 확장자는 실제로는 찾아야 함
					  .toList();
	}

}
