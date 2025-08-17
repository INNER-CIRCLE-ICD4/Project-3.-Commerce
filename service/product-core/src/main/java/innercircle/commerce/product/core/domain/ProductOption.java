package innercircle.commerce.product.core.domain.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class ProductOption {
	private Long id;
	private Long productId;
	private String name;
	private boolean isRequired;
	private int sortOrder;
	@Builder.Default
	private List<ProductOptionItem> items = Collections.emptyList();

	/**
	 * 옵션에 유효한 아이템들이 있는지 확인합니다.
	 *
	 * @return 아이템이 1개 이상 있으면 true, 그렇지 않으면 false
	 */
	public boolean hasValidItems () {
		return !CollectionUtils.isEmpty(items);
	}

	/**
	 * 옵션 리스트의 유효성을 검증합니다.
	 * 옵션이 존재하는 경우 최소 1개 이상의 아이템이 필요합니다.
	 *
	 * @param options 검증할 옵션 리스트
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static void validateOptions (List<ProductOption> options) {
		if (CollectionUtils.isEmpty(options)) {
			return;
		}

		for (ProductOption option : options) {
			if (!option.hasValidItems()) {
				throw new IllegalArgumentException("옵션이 존재하는 경우 최소 1개 이상의 옵션 속성이 필요합니다");
			}
		}
	}
}
