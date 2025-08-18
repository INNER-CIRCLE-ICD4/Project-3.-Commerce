package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static innercircle.commerce.product.core.domain.ProductImageValidator.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductImage {
	private Long id;
	private Long productId;
	private String url;
	private String originalName;
	private boolean isMain;
	private int sortOrder;

	private ProductImage (
			Long id, Long productId, String url,
			String originalName, boolean isMain, int sortOrder
	) {
		this.id = id == null ? IdGenerator.generateId() : id;
		this.setProductId(productId);
		this.setUrl(url);
		this.setOriginalName(originalName);
		this.isMain = isMain;
		this.setSortOrder(sortOrder);
	}

	/**
	 * 상품 이미지를 생성합니다.
	 * ID가 제공되면 해당 ID를 사용하고, 없으면 Snowflake를 통해 새로운 ID를 생성합니다.
	 *
	 * @param productId    상품 ID
	 * @param url          이미지 URL
	 * @param originalName 원본 파일명
	 * @param isMain       대표 이미지 여부
	 * @param sortOrder    정렬 순서
	 * @return 생성된 상품 이미지 객체
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static ProductImage create (
			Long productId, String url, String originalName, boolean isMain, int sortOrder
	) {
		return new ProductImage(null, productId, url, originalName, isMain, sortOrder);
	}

	protected void setProductId (Long productId) {
		validateProductId(productId);
		this.productId = productId;
	}

	private void setUrl (String url) {
		validateUrl(url);
		this.url = url;
	}

	private void setOriginalName (String originalName) {
		validateOriginalName(originalName);
		this.originalName = originalName;
	}

	private void setSortOrder (int sortOrder) {
		validateSortOrder(sortOrder);
		this.sortOrder = sortOrder;
	}
}
