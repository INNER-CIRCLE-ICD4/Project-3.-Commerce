package innercircle.commerce.product.core.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 상품 삭제 도메인 이벤트
 */
@Getter
@RequiredArgsConstructor
public class ProductDeletedEvent {
    
    private final Long productId;
    private final List<String> imageUrls;
}