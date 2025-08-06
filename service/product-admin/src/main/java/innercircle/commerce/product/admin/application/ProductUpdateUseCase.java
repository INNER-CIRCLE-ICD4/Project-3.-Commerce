package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductImageUpdateCommand;
import innercircle.commerce.product.admin.application.dto.ProductSaleTypeChangeCommand;
import innercircle.commerce.product.admin.application.dto.ProductStatusChangeCommand;
import innercircle.commerce.product.admin.application.dto.ProductUpdateCommand;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.admin.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.entity.Product;

/**
 * 상품 수정 UseCase
 */
public class ProductUpdateUseCase {
    
    private final ProductRepository productRepository;
    
    public ProductUpdateUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * 상품 기본 정보를 수정합니다.
     * 
     * @param command 상품 수정 명령
     * @return 수정된 상품
     * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
     * @throws DuplicateProductNameException 상품명이 중복된 경우
     */
    public Product updateBasicInfo(ProductUpdateCommand command) {
        // 1. 상품 존재 검증
        Product product = findProductById(command.productId());
        
        // 2. 상품명 중복 검증 (자신 제외)
        validateProductNameDuplicateForUpdate(command.name(), command.productId());
        
        // 3. 도메인 객체 수정 (도메인 검증 로직 포함)
        product.updateBasicInfo(command.name(), command.basePrice(), command.detailContent());
        
        // 4. 상품 저장
        return productRepository.save(product);
    }
    
    /**
     * 상품 상태를 변경합니다.
     * 
     * @param command 상품 상태 변경 명령
     * @return 상태가 변경된 상품
     * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
     */
    public Product changeStatus(ProductStatusChangeCommand command) {
        Product product = findProductById(command.productId());
        
        // 새로운 API에 맞게 상태별로 구분하여 호출
        switch (command.status()) {
            case SALE -> product.changeToSale();
            case OUTOFSTOCK -> product.changeToOutOfStock();
            case CLOSE -> product.changeToClose();
        }
        
        return productRepository.save(product);
    }
    
    /**
     * 상품 판매 유형을 변경합니다.
     * 
     * @param command 상품 판매 유형 변경 명령
     * @return 판매 유형이 변경된 상품
     * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
     */
    public Product changeSaleType(ProductSaleTypeChangeCommand command) {
        Product product = findProductById(command.productId());
        
        // 새로운 API에 맞게 판매 타입별로 구분하여 호출
        switch (command.saleType()) {
            case NEW -> product.changeToNew();
            case OLD -> product.changeToOld();
        }
        
        return productRepository.save(product);
    }
    
    /**
     * 상품 이미지를 수정합니다.
     * 
     * @param command 상품 이미지 수정 명령
     * @return 이미지가 수정된 상품
     * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
     */
    public Product updateImages(ProductImageUpdateCommand command) {
        Product product = findProductById(command.productId());
        product.updateImages(command.images());
        
        return productRepository.save(product);
    }
    
    /**
     * 상품 ID로 상품을 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 조회된 상품
     * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
     */
    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
    
    /**
     * 상품 수정 시 상품명 중복 여부를 검증합니다.
     * 
     * @param productName 상품명
     * @param excludeProductId 제외할 상품 ID (자신의 ID)
     * @throws DuplicateProductNameException 중복된 경우
     */
    private void validateProductNameDuplicateForUpdate(String productName, Long excludeProductId) {
        if (productRepository.existsByNameAndIdNot(productName, excludeProductId)) {
            throw new DuplicateProductNameException(productName);
        }
    }
}
