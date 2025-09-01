package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ProductAdminInfo;
import innercircle.commerce.product.admin.application.dto.ProductListAdminInfo;
import innercircle.commerce.product.admin.application.dto.ProductListQuery;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.core.application.repository.ProductRepository;
import innercircle.commerce.product.core.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 조회 UseCase
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductRetrieveUseCase {
    
    private final ProductRepository productRepository;
    
    /**
     * 상품 목록을 조회합니다.
     * 
     * @param query 조회 조건
     * @return 상품 목록
     */
    public Page<ProductListAdminInfo> getProducts(ProductListQuery query) {
        Page<Product> products = productRepository.findProducts(
                query.getStatus(), 
                query.getCategoryId(), 
                query.getPageable()
        );
        return products.map(ProductListAdminInfo::from);
    }
    
    /**
     * 상품 상세 정보를 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 상품 정보
     * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
     */
    public ProductAdminInfo getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductAdminInfo.from(product);
    }
}