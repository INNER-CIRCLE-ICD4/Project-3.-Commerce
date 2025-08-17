package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
		setImages(images);
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
	 * @param images        상품 이미지 리스트
	 * @param detailContent 상품 상세 내용
	 * @return 생성된 상품 객체
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static Product create (
			String name, Long categoryId, Long brandId, Integer price,
			Integer stock, List<ProductOption> options, List<ProductImage> images, String detailContent
	) {
		LocalDateTime now = LocalDateTime.now();
		return new Product(
				null, name, categoryId, brandId, price, stock, options, images,
				detailContent, SaleType.NEW, ProductStatus.SALE, now, now
		);
	}

	public static Product restore (
			Long id, String name, Long categoryId, Long brandId, Integer price, Integer stock, List<ProductOption> options,
			List<ProductImage> images, String detailContent, SaleType saleType, ProductStatus status
	) {
		return new Product(
				id, name, categoryId, brandId, price, stock, options,
				images, detailContent, saleType, status, null, null
		);
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
		this.options = Collections.unmodifiableList(options);
	}

	private void setImages (List<ProductImage> images) {
		validateImages(images);
		this.images = images;
	}

	private void setDetailContent (String detailContent) {
		validateDetailContent(detailContent);
		this.detailContent = detailContent;
	}

	/**
	 * 상품에 이미지 리스트를 추가합니다.
	 *
	 * @param images 추가할 이미지 리스트
	 */
	public void addImages (List<ProductImage> images) {
		setImages(images);
	}
}
