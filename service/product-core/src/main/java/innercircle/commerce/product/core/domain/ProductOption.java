package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
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
	private ProductStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private ProductOption (
			Long id, Long productId, String name, boolean isRequired,
			int sortOrder, List<ProductOptionItem> items, ProductStatus status,
			LocalDateTime createdAt, LocalDateTime updatedAt
	) {
		this.id = id == null ? IdGenerator.generateId() : id;
		this.setProductId(productId);
		this.setName(name);
		this.isRequired = isRequired;
		this.setSortOrder(sortOrder);
		this.setItems(items);
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
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
		LocalDateTime now = LocalDateTime.now();
		return new ProductOption(null, productId, name, isRequired, sortOrder, items, ProductStatus.SALE, now, now);
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
	 * @param status     상태
	 * @param createdAt  생성일시
	 * @param updatedAt  수정일시
	 * @return 복원된 상품 옵션 객체
	 */
	public static ProductOption restore (
			Long id, Long productId, String name, boolean isRequired, int sortOrder, 
			List<ProductOptionItem> items, ProductStatus status, LocalDateTime createdAt, LocalDateTime updatedAt
	) {
		return new ProductOption(id, productId, name, isRequired, sortOrder, items, status, createdAt, updatedAt);
	}

	/**
	 * 기존 상품 옵션을 복원합니다. (하위 호환성을 위한 오버로드 메서드)
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
		LocalDateTime now = LocalDateTime.now();
		return new ProductOption(id, productId, name, isRequired, sortOrder, items, ProductStatus.SALE, now, now);
	}

	/**
	 * 상품 옵션을 논리 삭제합니다.
	 */
	public void delete() {
		if (this.status == ProductStatus.DELETE) {
			throw new IllegalArgumentException("이미 삭제된 옵션입니다.");
		}
		
		this.status = ProductStatus.DELETE;
		this.updatedAt = LocalDateTime.now();
		
		// 옵션 아이템들도 함께 삭제
		if (!CollectionUtils.isEmpty(this.items)) {
			this.items.forEach(item -> {
				if (item.getStatus() != ProductStatus.DELETE) {
					item.delete();
				}
			});
		}
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
