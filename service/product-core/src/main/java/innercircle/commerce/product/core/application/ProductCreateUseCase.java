package innercircle.commerce.product.core.application;

import innercircle.commerce.product.core.application.dto.ProductCreateCommand;
import innercircle.commerce.product.core.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.core.application.exception.InvalidBrandException;
import innercircle.commerce.product.core.application.exception.InvalidCategoryException;
import innercircle.commerce.product.core.application.repository.BrandRepository;
import innercircle.commerce.product.core.application.repository.CategoryRepository;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.entity.Product;

/**
 * 상품 등록 UseCase
 */
public class ProductCreateUseCase {
    
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    
    public ProductCreateUseCase(
            ProductRepository productRepository,
            BrandRepository brandRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }
    
    /**
     * 상품을 등록합니다.
     * 
     * @param command 상품 등록 명령
     * @return 등록된 상품
     * @throws DuplicateProductNameException 상품명이 중복된 경우
     * @throws InvalidBrandException 유효하지 않은 브랜드 ID인 경우
     * @throws InvalidCategoryException 유효하지 않은 카테고리 ID인 경우
     */
    public Product create(ProductCreateCommand command) {
        validateProductNameDuplicate(command.name());
        validateBrandExists(command.brandId());
        validateCategoryExists(command.leafCategoryId());
        
        Product product = command.toDomain();
        
        return productRepository.save(product);
    }
    
    /**
     * 상품명 중복 여부를 검증합니다.
     * 
     * @param productName 상품명
     * @throws DuplicateProductNameException 중복된 경우
     */
    private void validateProductNameDuplicate(String productName) {
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
    private void validateBrandExists(Long brandId) {
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
    private void validateCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new InvalidCategoryException(categoryId);
        }
    }
}
