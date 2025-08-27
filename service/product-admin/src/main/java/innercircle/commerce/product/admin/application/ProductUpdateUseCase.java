package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductUpdateCommand;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductImage;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 상품 수정 UseCase
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductUpdateUseCase {

	private final ProductRepository productRepository;
	private final S3ImageStore s3ImageStore;

	/**
	 * 상품 정보를 수정합니다. (기본 정보 + 이미지 변경사항)
	 *
	 * @param command 상품 수정 명령
	 * @return 수정된 상품
	 * @throws ProductNotFoundException      상품을 찾을 수 없는 경우
	 * @throws DuplicateProductNameException 상품명이 중복된 경우
	 */
	public Product updateProduct(ProductUpdateCommand command) {
		Product product = findProductById(command.productId());

		validateProductNameDuplicateForUpdate(command.name(), command.productId());

		product.update(command.name(), command.basePrice(), command.detailContent());

		if (!CollectionUtils.isEmpty(command.imagesToAdd())) {
			List<ProductImage> finalImages = moveImagesToFinalPath(command.imagesToAdd(), command.productId());
			
			product.addImages(finalImages);
		}

		if (!CollectionUtils.isEmpty(command.imagesToDelete())) {
			for (String imageUrl : command.imagesToDelete()) {
				product.removeImageByUrl(imageUrl);

				String s3Key = extractS3KeyFromUrl(imageUrl);
				s3ImageStore.delete(s3Key);
			}
		}

		return productRepository.save(product);
	}



	/**
	 * 상품 ID로 상품을 조회합니다.
	 *
	 * @param productId 상품 ID
	 * @return 조회된 상품
	 * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
	 */
	private Product findProductById (Long productId) {
		return productRepository.findById(productId)
								.orElseThrow(() -> new ProductNotFoundException(productId));
	}

	/**
	 * 상품 수정 시 상품명 중복 여부를 검증합니다.
	 *
	 * @param productName      상품명
	 * @param excludeProductId 제외할 상품 ID (자신의 ID)
	 * @throws DuplicateProductNameException 중복된 경우
	 */
	private void validateProductNameDuplicateForUpdate (String productName, Long excludeProductId) {
		if (productRepository.existsByNameAndIdNot(productName, excludeProductId)) {
			throw new DuplicateProductNameException(productName);
		}
	}

	/**
	 * 임시 경로의 이미지들을 실제 경로로 이동합니다.
	 * 
	 * @param tempImages 임시 경로의 이미지 목록
	 * @param productId 상품 ID
	 * @return 실제 경로로 이동된 이미지 목록
	 */
	private List<ProductImage> moveImagesToFinalPath(List<ProductImage> tempImages, Long productId) {
		return tempImages.stream()
			.map(image -> {
				// 임시 경로에서 실제 경로로 S3 이미지 이동
				String tempKey = extractS3KeyFromUrl(image.getUrl());
				String extension = extractExtensionFromOriginalName(image.getOriginalName());
				String finalKey = buildFinalImageKey(productId, image.getId(), extension);
				
				Optional<String> finalUrl = s3ImageStore.move(tempKey, finalKey);
				
				return ProductImage.restore(
					image.getId(), // 기존 이미지 ID 유지
					productId,
					finalUrl.orElse(image.getUrl()), // 이동 실패 시 원본 URL 유지
					image.getOriginalName(),
					image.getSortOrder()
				);
			})
			.collect(Collectors.toList());
	}

	/**
	 * URL에서 S3 키를 추출합니다.
	 * 예: https://bucket.s3.region.amazonaws.com/commerce/temp/123/image.jpg → commerce/temp/123/image.jpg
	 */
	private String extractS3KeyFromUrl(String url) {
		// 간단한 URL 파싱 (향후 개선 가능)
		int keyStartIndex = url.indexOf(".com/") + 5;
		return url.substring(keyStartIndex);
	}

	/**
	 * 원본 파일명에서 확장자를 추출합니다.
	 *
	 * @param originalName 원본 파일명
	 * @return 확장자 (점 제외)
	 */
	private String extractExtensionFromOriginalName(String originalName) {
		if (originalName != null && originalName.contains(".")) {
			String[] nameParts = originalName.split("\\.");
			return nameParts[nameParts.length - 1].toLowerCase();
		}
		return "jpg";
	}

	/**
	 * 상품의 최종 이미지 경로를 생성합니다.
	 * 예: commerce/products/123/456.jpg (이미지 ID 기반)
	 */
	private String buildFinalImageKey(Long productId, Long imageId, String extension) {
		return String.format("commerce/products/%d/%d.%s", productId, imageId, extension);
	}
}
