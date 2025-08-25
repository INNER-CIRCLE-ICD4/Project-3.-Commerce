package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static innercircle.commerce.product.core.domain.ProductValidator.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
	private Long id;
	private String name;
	private Long categoryId;
	private Long brandId;
	private Integer price;
	private ProductStatus status;
	private SaleType saleType;
	private List<ProductOption> options;
	private List<ProductImage> images;
	private String detailContent;
	private Integer stock;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder(access = AccessLevel.PRIVATE)
	private Product (
			Long id, String name, Long categoryId, Long brandId, Integer price, Integer stock,
			List<ProductOption> options, List<ProductImage> images, String detailContent,
			SaleType saleType, ProductStatus status, LocalDateTime createdAt, LocalDateTime updatedAt
	) {
		this.id = id == null ? IdGenerator.generateId() : id;
		setName(name);
		setCategoryId(categoryId);
		setBrandId(brandId);
		setPrice(price);
		setStock(stock);
		setOptions(options);
		this.images = images;
		setDetailContent(detailContent);
		this.saleType = saleType;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * 상품을 생성합니다.
	 * ID가 제공되면 해당 ID를 사용하고, 없으면 Snowflake를 통해 새로운 ID를 생성합니다.
	 *
	 * @param name          상품명
	 * @param categoryId    최하위 카테고리 ID
	 * @param brandId       브랜드 ID
	 * @param price         기본 가격
	 * @param stock         재고 수량
	 * @param options       상품 옵션 리스트 (nullable)
	 * @param detailContent 상품 상세 내용
	 * @return 생성된 상품 객체
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static Product create (
			String name, Long categoryId, Long brandId, Integer price,
			Integer stock, List<ProductOption> options, String detailContent
	) {
		LocalDateTime now = LocalDateTime.now();

		return Product.builder()
					  .id(null)
					  .name(name)
					  .categoryId(categoryId)
					  .brandId(brandId)
					  .price(price)
					  .stock(stock)
					  .options(options)
					  .detailContent(detailContent)
					  .saleType(SaleType.NEW)
					  .status(ProductStatus.SALE)
					  .createdAt(now)
					  .updatedAt(now)
					  .build();
	}

	public static Product restore (
			Long id, String name, Long categoryId, Long brandId, Integer price, Integer stock, List<ProductOption> options,
			List<ProductImage> images, String detailContent, SaleType saleType, ProductStatus status
	) {
		return Product.builder()
					  .id(id)
					  .name(name)
					  .categoryId(categoryId)
					  .brandId(brandId)
					  .price(price)
					  .stock(stock)
					  .options(options)
					  .images(images)
					  .detailContent(detailContent)
					  .saleType(saleType)
					  .status(status)
					  .build();
	}

	public void addImages (List<ProductImage> images) {
		if (images == null || images.isEmpty()) {
			throw new IllegalArgumentException("추가할 이미지는 최소 1개 이상이어야 합니다.");
		}

		if (this.images == null) {
			// 첫 등록시에는 전체 검증 수행
			validateImages(images);
			this.images = images;
			return;
		}

		// 기존 이미지 + 새 이미지 총합 검증
		int totalImageCount = this.images.size() + images.size();
		if (totalImageCount > 6) {
			throw new IllegalArgumentException("상품 이미지는 6개까지 등록할 수 있습니다.");
		}

		// 기존 이미지에 새 이미지 추가
		List<ProductImage> newImages = new ArrayList<>(this.images);
		newImages.addAll(images);
		this.images = newImages;
	}


	public void removeImageByUrl (String imageUrl) {
		if (images == null || images.isEmpty()) {
			throw new IllegalArgumentException("삭제할 이미지가 없습니다.");
		}

		// 해당 URL의 이미지가 있는지 확인
		boolean targetImageExists = images.stream()
				.anyMatch(image -> image.getUrl().equals(imageUrl));
		
		if (!targetImageExists) {
			throw new IllegalArgumentException("해당 URL의 이미지를 찾을 수 없습니다.");
		}

		// 삭제 후 남은 이미지들
		List<ProductImage> remainingImages = images.stream()
				.filter(image -> !image.getUrl().equals(imageUrl))
				.toList();

		if (remainingImages.isEmpty()) {
			throw new IllegalArgumentException("상품 이미지는 필수 입니다.");
		}

		boolean hasMainImageAfterDelete = remainingImages.stream()
				.anyMatch(ProductImage::isMain);

		if (!hasMainImageAfterDelete) {
			throw new IllegalArgumentException("메인 이미지는 필수 입니다.");
		}

		this.images = remainingImages;
	}

	public void update (String name, Integer price, String detailContent) {
		this.setName(name);
		this.setPrice(price);
		this.setDetailContent(detailContent);
	}

	private void setName (String name) {
		validateProductName(name);
		this.name = name;
	}

	private void setCategoryId (Long categoryId) {
		validateCategoryId(categoryId);
		this.categoryId = categoryId;
	}

	private void setBrandId (Long brandId) {
		validateBrandId(brandId);
		this.brandId = brandId;
	}

	private void setPrice (Integer price) {
		validatePrice(price);
		this.price = price;
	}

	private void setStock (Integer stock) {
		validateStock(stock);
		this.stock = stock;
	}

	private void setOptions (List<ProductOption> options) {
		validateOptions(options);
		this.options = Collections.unmodifiableList(options != null ? options : Collections.emptyList());
	}

	private void setDetailContent (String detailContent) {
		validateDetailContent(detailContent);
		this.detailContent = detailContent;
	}
}
