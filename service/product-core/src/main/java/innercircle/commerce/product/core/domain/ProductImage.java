package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static innercircle.commerce.product.core.domain.ProductImageValidator.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductImage {
	private Long id;
	private Long productId;
	private String url;
	private String originalName;
	private int sortOrder;
	private ProductStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private ProductImage (
			Long id, Long productId, String url,
			String originalName, int sortOrder, ProductStatus status,
			LocalDateTime createdAt, LocalDateTime updatedAt
	) {
		this.id = id == null ? IdGenerator.generateId() : id;
		this.setProductId(productId);
		this.setUrl(url);
		this.setOriginalName(originalName);
		this.setSortOrder(sortOrder);
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * 상품 이미지를 생성합니다.
	 * ID가 제공되면 해당 ID를 사용하고, 없으면 Snowflake를 통해 새로운 ID를 생성합니다.
	 *
	 * @param productId    상품 ID
	 * @param url          이미지 URL
	 * @param originalName 원본 파일명
	 * @param sortOrder    정렬 순서
	 * @return 생성된 상품 이미지 객체
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static ProductImage create (
			Long productId, String url, String originalName, int sortOrder
	) {
		LocalDateTime now = LocalDateTime.now();
		return new ProductImage(null, productId, url, originalName, sortOrder, ProductStatus.SALE, now, now);
	}

	/**
	 * 기존 상품 이미지를 복원합니다.
	 * 주로 데이터베이스에서 조회한 데이터로 객체를 복원할 때 사용됩니다.
	 *
	 * @param id           상품 이미지 ID
	 * @param productId    상품 ID
	 * @param url          이미지 URL
	 * @param originalName 원본 파일명
	 * @param sortOrder    정렬 순서
	 * @param status       상태
	 * @param createdAt    생성일시
	 * @param updatedAt    수정일시
	 * @return 복원된 상품 이미지 객체
	 */
	public static ProductImage restore (
			Long id, Long productId, String url, String originalName, int sortOrder,
			ProductStatus status, LocalDateTime createdAt, LocalDateTime updatedAt
	) {
		return new ProductImage(id, productId, url, originalName, sortOrder, status, createdAt, updatedAt);
	}

	/**
	 * 기존 상품 이미지를 복원합니다. (하위 호환성을 위한 오버로드 메서드)
	 * 
	 * @param id           상품 이미지 ID
	 * @param productId    상품 ID
	 * @param url          이미지 URL
	 * @param originalName 원본 파일명
	 * @param sortOrder    정렬 순서
	 * @return 복원된 상품 이미지 객체
	 */
	public static ProductImage restore (
			Long id, Long productId, String url, String originalName, int sortOrder
	) {
		LocalDateTime now = LocalDateTime.now();
		return new ProductImage(id, productId, url, originalName, sortOrder, ProductStatus.SALE, now, now);
	}

	/**
	 * 상품 이미지를 논리 삭제합니다.
	 */
	public void delete() {
		if (this.status == ProductStatus.DELETE) {
			throw new IllegalArgumentException("이미 삭제된 이미지입니다.");
		}
		
		this.status = ProductStatus.DELETE;
		this.updatedAt = LocalDateTime.now();
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
