package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductImageUpdateCommand;
import innercircle.commerce.product.admin.application.dto.ProductSaleTypeChangeCommand;
import innercircle.commerce.product.admin.application.dto.ProductStatusChangeCommand;
import innercircle.commerce.product.admin.application.dto.ProductUpdateCommand;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 상품 수정 UseCase
 */
@Service
@RequiredArgsConstructor
public class ProductUpdateUseCase {

	private final ProductRepository productRepository;

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
}
