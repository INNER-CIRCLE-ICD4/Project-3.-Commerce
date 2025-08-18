package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

import static innercircle.commerce.product.core.domain.ProductOptionValidator.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductOption {
	private Long id;
	private Long productId;
	private String name;
	private boolean isRequired;
	private int sortOrder;
	private List<ProductOptionItem> items;

	private ProductOption (
			Long id, Long productId, String name, boolean isRequired,
			int sortOrder, List<ProductOptionItem> items
	) {
		this.id = id == null ? IdGenerator.generateId() : id;
		this.setProductId(productId);
		this.setName(name);
		this.isRequired = isRequired;
		this.setSortOrder(sortOrder);
		this.setItems(items);
	}

	/**
	 * 상품 옵션을 생성합니다.
	 * ID가 제공되면 해당 ID를 사용하고, 없으면 Snowflake를 통해 새로운 ID를 생성합니다.
	 *
	 * @param productId  상품 ID
	 * @param name       옵션명
	 * @param isRequired 필수 여부
	 * @param sortOrder  정렬 순서
	 * @param items      옵션 아이템 리스트
	 * @return 생성된 상품 옵션 객체
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static ProductOption create (
			Long productId, String name, boolean isRequired, int sortOrder, List<ProductOptionItem> items
	) {
		return new ProductOption(null, productId, name, isRequired, sortOrder, items);
	}

	/**
	 * 기존 상품 옵션을 복원합니다.
	 * 주로 데이터베이스에서 조회한 데이터로 객체를 복원할 때 사용됩니다.
	 *
	 * @param id         상품 옵션 ID
	 * @param productId  상품 ID
	 * @param name       옵션명
	 * @param isRequired 필수 여부
	 * @param sortOrder  정렬 순서
	 * @param items      옵션 아이템 리스트
	 * @return 복원된 상품 옵션 객체
	 */
	public static ProductOption restore (
			Long id, Long productId, String name, boolean isRequired, int sortOrder, List<ProductOptionItem> items
	) {
		return new ProductOption(id, productId, name, isRequired, sortOrder, items);
	}

	private void setProductId (Long productId) {
		validateProductId(productId);
		this.productId = productId;
	}

	private void setName (String name) {
		validateName(name);
		this.name = name;
	}

	private void setSortOrder (int sortOrder) {
		validateSortOrder(sortOrder);
		this.sortOrder = sortOrder;
	}

	private void setItems (List<ProductOptionItem> items) {
		validateItems(items);
		this.items = items;
	}
}
