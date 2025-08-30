package innercircle.commerce.product.admin.application.dto;

import innercircle.commerce.product.core.domain.ProductStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

/**
 * 상품 목록 조회 조건
 */
@Getter
@Builder
public class ProductListQuery {
    private final ProductStatus status;
    private final Long categoryId;
    private final Pageable pageable;
}