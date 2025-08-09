package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ImageUploadCommand;
import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.InvalidBrandException;
import innercircle.commerce.product.admin.application.exception.InvalidCategoryException;
import innercircle.commerce.product.admin.application.exception.NotFoundTempImageException;
import innercircle.commerce.product.admin.application.repository.BrandRepository;
import innercircle.commerce.product.admin.application.repository.CategoryRepository;
import innercircle.commerce.product.admin.application.repository.ProductRepository;
import innercircle.commerce.product.admin.application.validator.ImageValidator;
import innercircle.commerce.product.core.domain.entity.Product;
import innercircle.commerce.product.core.domain.entity.ProductImage;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 상품 등록 UseCase
 */
@Service
public class ProductCreateUseCase {

	private final ProductRepository productRepository;
	private final BrandRepository brandRepository;
	private final CategoryRepository categoryRepository;
	private final S3ImageStore s3ImageStore;
	private final ImageValidator imageValidator;

	public ProductCreateUseCase (
			ProductRepository productRepository,
			BrandRepository brandRepository,
			CategoryRepository categoryRepository,
			S3ImageStore s3ImageStore,
			ImageValidator imageValidator
	) {
		this.productRepository = productRepository;
		this.brandRepository = brandRepository;
		this.categoryRepository = categoryRepository;
		this.s3ImageStore = s3ImageStore;
		this.imageValidator = imageValidator;
	}

	/**
	 * 이미지와 함께 상품을 등록합니다.
	 *
	 * @param command 상품 등록 명령 (이미지 포함)
	 * @return 등록된 상품
	 * @throws DuplicateProductNameException 상품명이 중복된 경우
	 * @throws InvalidBrandException         유효하지 않은 브랜드 ID인 경우
	 * @throws InvalidCategoryException      유효하지 않은 카테고리 ID인 경우
	 */
	public Product create (ProductCreateCommand command) throws IOException {
		// 1. 기본 검증
		validateProductNameDuplicate(command.name());
		validateBrandExists(command.brandId());
		validateCategoryExists(command.leafCategoryId());

		// 2. 이미지 검증
		for (var file : command.imageCommand().files()) {
			imageValidator.validate(file);
		}

		// 3. 임시 이미지 업로드 (실패 시 전체 프로세스 중단)
		List<String> tempIds = uploadImagesToTemp(command.imageCommand());

		try {
			// 4. 상품을 먼저 생성하여 ID 확보 (임시)
			Product tempProduct = Product.builder()
										 .name("TEMP_FOR_ID_" + UUID.randomUUID())
										 .leafCategoryId(1L)
										 .brandId(1L)
										 .basePrice(1)
										 .stock(1)
										 .options(Collections.emptyList())
										 .images(Collections.emptyList())
										 .detailContent("TEMP")
										 .build();
			Product savedTempProduct = productRepository.save(tempProduct);
			Long productId = savedTempProduct.getId();

			// 5. 임시 이미지를 상품 경로로 이동 (실패 시 롤백)
			List<ProductImage> productImages = moveImagesToProduct(productId, tempIds, command.imageCommand());

			// 6. 완성된 이미지로 실제 상품 데이터 업데이트
			Product actualProduct = Product.create(
					command.name(),
					command.leafCategoryId(),
					command.brandId(),
					command.basePrice(),
					command.stock(),
					command.options() != null ? command.options() : Collections.emptyList(),
					productImages,
					command.detailContent()
			);

			// ID와 타임스탬프는 기존 것 유지
			Product finalProduct = Product.builder()
										  .id(productId)
										  .name(actualProduct.getName())
										  .leafCategoryId(actualProduct.getLeafCategoryId())
										  .brandId(actualProduct.getBrandId())
										  .basePrice(actualProduct.getBasePrice())
										  .stock(actualProduct.getStock())
										  .options(actualProduct.getOptions())
										  .images(actualProduct.getImages())
										  .detailContent(actualProduct.getDetailContent())
										  .saleType(actualProduct.getSaleType())
										  .status(actualProduct.getStatus())
										  .createdAt(savedTempProduct.getCreatedAt())
										  .updatedAt(LocalDateTime.now())
										  .build();

			return productRepository.save(finalProduct);

		} catch (Exception e) {
			// 실패 시 임시 이미지 정리
			s3ImageStore.delete(buildTempKeys(tempIds));
			throw e;
		}
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
	 * 이미지들을 임시 경로에 업로드합니다.
	 */
	private List<String> uploadImagesToTemp (ImageUploadCommand imageUploadCommand) throws IOException {
		List<String> tempIds = new ArrayList<>();

		for (int i = 0; i < imageUploadCommand.files().size(); i++) {
			var file = imageUploadCommand.files().get(i);
			String tempId = UUID.randomUUID().toString();
			String s3Key = buildTempPath(tempId, getFileExtension(file.getOriginalFilename()));

			s3ImageStore.upload(file, s3Key);
			tempIds.add(tempId);
		}

		return tempIds;
	}

	/**
	 * 임시 이미지들을 상품 이미지로 이동합니다.
	 */
	private List<ProductImage> moveImagesToProduct (Long productId, List<String> tempIds, ImageUploadCommand imageUploadCommand) {
		List<ProductImage> productImages = new ArrayList<>();

		for (int i = 0; i < tempIds.size(); i++) {
			String tempId = tempIds.get(i);
			var metadata = imageUploadCommand.metadata().get(i);

			String tempKey = buildTempPath(tempId, getFileExtension(imageUploadCommand.files().get(i).getOriginalFilename()));
			String productKey = buildProductPath(productId, (long) (i + 1), getFileExtension(imageUploadCommand.files().get(i).getOriginalFilename()));

			var movedUrl = s3ImageStore.move(tempKey, productKey);
			if (movedUrl.isEmpty()) {
				throw new NotFoundTempImageException(tempId);
			}

			ProductImage productImage = ProductImage.builder()
													.id((long) (i + 1))
													.productId(productId)
													.url(movedUrl.get())
													.isMain(metadata.isMain())
													.sortOrder(metadata.sortOrder())
													.build();

			productImages.add(productImage);
		}

		return productImages;
	}

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

	/**
	 * 파일명에서 확장자 추출
	 */
	private String getFileExtension (String filename) {
		if (filename == null) return "jpg";
		int lastDotIndex = filename.lastIndexOf('.');
		if (lastDotIndex == -1) return "jpg";
		return filename.substring(lastDotIndex + 1).toLowerCase();
	}
}
