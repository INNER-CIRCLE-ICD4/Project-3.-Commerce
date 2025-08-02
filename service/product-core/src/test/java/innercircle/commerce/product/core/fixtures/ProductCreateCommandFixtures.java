package innercircle.commerce.product.core.fixtures;

import innercircle.commerce.product.core.application.dto.ProductCreateCommand;
import innercircle.commerce.product.core.domain.entity.ProductImage;
import innercircle.commerce.product.core.domain.entity.ProductOption;

import java.util.List;

import static innercircle.commerce.product.core.fixtures.ProductFixtures.*;

public class ProductCreateCommandFixtures {
    
    public static ProductCreateCommand createValidCommand() {
        return new ProductCreateCommand(
                VALID_NAME,
                VALID_CATEGORY_ID,
                VALID_BRAND_ID,
                VALID_BASE_PRICE,
                VALID_STOCK,
                ProductImageFixtures.createValidImages(),
                VALID_DETAIL_CONTENT,
                null
        );
    }
    
    public static ProductCreateCommand createValidCommandWithOptions() {
        return new ProductCreateCommand(
                VALID_NAME,
                VALID_CATEGORY_ID,
                VALID_BRAND_ID,
                VALID_BASE_PRICE,
                VALID_STOCK,
                ProductImageFixtures.createValidImages(),
                VALID_DETAIL_CONTENT,
                ProductOptionFixtures.createValidOptions()
        );
    }
}
