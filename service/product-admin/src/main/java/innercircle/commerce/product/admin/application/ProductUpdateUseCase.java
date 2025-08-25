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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 상품 수정 UseCase
 */
@Service
@RequiredArgsConstructor
public class ProductUpdateUseCase {

	private final ProductRepository productRepository;
	private final S3ImageStore s3ImageStore;

	/**
	 * 상품 기본 정보를 수정합니다.
	 *
	 * @param command 상품 수정 명령
	 * @return 수정된 상품
	 * @throws ProductNotFoundException      상품을 찾을 수 없는 경우
	 * @throws DuplicateProductNameException 상품명이 중복된 경우
	 */
	public Product updateBasicInfo (ProductUpdateCommand command) {
		Product product = findProductById(command.productId());

		// 2. 상품명 중복 검증 (자신 제외)
		validateProductNameDuplicateForUpdate(command.name(), command.productId());

		// 3. 도메인 객체 수정 (도메인 검증 로직 포함)
		product.update(command.name(), command.basePrice(), command.detailContent());

		// 4. 상품 저장
		return productRepository.save(product);
	}


	/**
	 * 상품의 특정 이미지를 URL을 기준으로 삭제합니다.
	 *
	 * @param productId 상품 ID
	 * @param imageUrl 삭제할 이미지 URL
	 * @return 수정된 상품
	 * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
	 */
	public Product deleteImage(Long productId, String imageUrl) {
		// 1. 상품 조회
		Product product = findProductById(productId);

		// 2. 도메인 객체에서 이미지 삭제 (도메인 검증 로직 포함)
		product.removeImageByUrl(imageUrl);

		// 3. S3에서 이미지 삭제
		String s3Key = extractS3KeyFromUrl(imageUrl);
		s3ImageStore.delete(s3Key);

		// 4. 상품 저장
		return productRepository.save(product);
	}

	/**
	 * 상품에 새로운 이미지들을 추가합니다. 
	 * 임시 저장소의 이미지들을 최종 경로로 이동한 후 상품에 추가합니다.
	 *
	 * @param productId 상품 ID
	 * @param tempImages 임시 저장소의 이미지 목록
	 * @return 수정된 상품
	 * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
	 */
	public Product addImages(Long productId, List<ProductImage> tempImages) {
		// 1. 상품 조회
		Product product = findProductById(productId);

		// 2. 임시 이미지들을 실제 경로로 이동
		List<ProductImage> finalImages = moveImagesToFinalPath(tempImages, productId);

		// 3. 도메인 객체에 이미지 추가 (도메인 검증 로직 포함)
		product.addImages(finalImages);

		// 4. 상품 저장
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
				String finalKey = buildFinalImageKey(productId, image.getOriginalName());
				
				Optional<String> finalUrl = s3ImageStore.move(tempKey, finalKey);
				
				return ProductImage.create(
					productId,
					finalUrl.orElse(image.getUrl()), // 이동 실패 시 원본 URL 유지
					image.getOriginalName(),
					image.isMain(),
					image.getSortOrder()
				);
			})
			.toList();
	}

	/**
	 * URL에서 S3 키를 추출합니다.
	 * 예: https://bucket.s3.region.amazonaws.com/commerce/temp/123/image.jpg -> commerce/temp/123/image.jpg
	 */
	private String extractS3KeyFromUrl(String url) {
		// 간단한 URL 파싱 (향후 개선 가능)
		int keyStartIndex = url.indexOf(".com/") + 5;
		return url.substring(keyStartIndex);
	}

	/**
	 * 상품의 최종 이미지 경로를 생성합니다.
	 * 예: commerce/products/123/image.jpg
	 */
	private String buildFinalImageKey(Long productId, String originalName) {
		return String.format("commerce/products/%d/%s", productId, originalName);
	}
}
