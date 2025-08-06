package innercircle.commerce.product.core.domain.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductOptionItem {
	private Long id;
	private Long optionId;
	private String name;
	private Integer additionalPrice;
	private int sortOrder;
}
