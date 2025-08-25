package innercircle.commerce.product.admin.fixtures;

import innercircle.commerce.product.admin.application.dto.ProductSaleTypeChangeCommand;
import innercircle.commerce.product.admin.application.dto.ProductStatusChangeCommand;
import innercircle.commerce.product.admin.application.dto.ProductUpdateCommand;
import innercircle.commerce.product.core.domain.ProductStatus;
import innercircle.commerce.product.core.domain.SaleType;

public class ProductUpdateCommandFixtures {
    
    public static final Long EXISTING_PRODUCT_ID = 1L;
    public static final String UPDATED_NAME = "수정된 상품명";
    public static final Integer UPDATED_PRICE = 15000;
    public static final String UPDATED_DETAIL_CONTENT = "<html>수정된 상품 설명</html>";
    
    public static ProductUpdateCommand createValidUpdateCommand() {
        return new ProductUpdateCommand(
                EXISTING_PRODUCT_ID,
                UPDATED_NAME,
                UPDATED_PRICE,
                UPDATED_DETAIL_CONTENT
        );
    }
    
    public static ProductUpdateCommand createUpdateCommandWithCustomValues(
            Long productId,
            String name,
            Integer basePrice,
            String detailContent
    ) {
        return new ProductUpdateCommand(
                productId,
                name,
                basePrice,
                detailContent
        );
    }
    
    public static ProductStatusChangeCommand createStatusChangeCommand() {
        return new ProductStatusChangeCommand(
                EXISTING_PRODUCT_ID,
                ProductStatus.CLOSE
        );
    }
    
    public static ProductStatusChangeCommand createStatusChangeCommand(Long productId, ProductStatus status) {
        return new ProductStatusChangeCommand(productId, status);
    }
    
    public static ProductSaleTypeChangeCommand createSaleTypeChangeCommand() {
        return new ProductSaleTypeChangeCommand(
                EXISTING_PRODUCT_ID,
                SaleType.OLD
        );
    }
    
    public static ProductSaleTypeChangeCommand createSaleTypeChangeCommand(Long productId, SaleType saleType) {
        return new ProductSaleTypeChangeCommand(productId, saleType);
    }
    
}
