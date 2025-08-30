package innercircle.commerce.product.admin.application.validator;

import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.InvalidBrandException;
import innercircle.commerce.product.admin.application.exception.InvalidCategoryException;
import innercircle.commerce.product.core.application.repository.BrandRepository;
import innercircle.commerce.product.core.application.repository.CategoryRepository;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 상품 등록 명령에 대한 검증을 담당하는 클래스
 */
@Component
@RequiredArgsConstructor
public class ProductCreateCommandValidator {

	private final ProductRepository productRepository;
	private final BrandRepository brandRepository;
	private final CategoryRepository categoryRepository;

	/**
	 * 상품 등록 명령에 대한 전체 검증을 수행합니다.
	 *
	 * @param command 상품 등록 명령
	 * @throws DuplicateProductNameException 상품명이 중복된 경우
	 * @throws InvalidBrandException         유효하지 않은 브랜드 ID인 경우
	 * @throws InvalidCategoryException      유효하지 않은 카테고리 ID인 경우
	 */
	public void validate(ProductCreateCommand command) {
		validateProductNameDuplicate(command.name());
		validateBrandExists(command.brandId());
		validateCategoryExists(command.leafCategoryId());
	}

	/**
	 * 상품명 중복 여부를 검증합니다.
	 *
	 * @param productName 상품명
	 * @throws DuplicateProductNameException 중복된 경우
	 */
	public void validateProductNameDuplicate(String productName) {
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
	public void validateBrandExists(Long brandId) {
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
	public void validateCategoryExists(Long categoryId) {
		if (!categoryRepository.existsById(categoryId)) {
			throw new InvalidCategoryException(categoryId);
		}
	}
}