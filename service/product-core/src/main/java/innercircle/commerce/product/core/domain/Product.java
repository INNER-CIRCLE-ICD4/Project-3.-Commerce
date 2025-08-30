package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

	public void addImages (List<ProductImage> newImages) {
		validateImages(newImages);

		List<ProductImage> currentImages = this.images != null ? this.images : new ArrayList<>();
		int totalImageCount = currentImages.size() + newImages.size();
		if (totalImageCount > 6) {
			throw new IllegalArgumentException("상품 이미지는 6개까지 등록할 수 있습니다.");
		}

		List<ProductImage> combinedImages = new ArrayList<>(currentImages);
		combinedImages.addAll(newImages);
		this.images = combinedImages;
		this.updatedAt = LocalDateTime.now();
	}


	public void removeImageByUrl (String imageUrl) {
		if (StringUtils.isBlank(imageUrl)) {
			throw new IllegalArgumentException("삭제할 이미지가 없습니다.");
		}

		boolean targetImageExists = images.stream()
				.anyMatch(image -> image.getUrl().equals(imageUrl));
		
		if (!targetImageExists) {
			throw new IllegalArgumentException("해당 URL의 이미지를 찾을 수 없습니다. :: " + imageUrl);
		}

		List<ProductImage> remainingImages = images.stream()
				.filter(image -> !image.getUrl().equals(imageUrl))
				.collect(Collectors.toCollection(ArrayList::new));

		if (remainingImages.isEmpty()) {
			throw new IllegalArgumentException("상품 이미지는 필수 입니다.");
		}
		remainingImages.forEach(image -> System.out.println("삭제된 이미지 :: " + image.getId() + ", " + image.getProductId()));
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

	/**
	 * 상품을 삭제 상태로 변경합니다.
	 * 연관된 이미지와 옵션들도 함께 삭제 상태로 변경됩니다.
	 * 
	 * @throws IllegalArgumentException 이미 삭제된 상품인 경우
	 */
	public void delete() {
		if (this.status == ProductStatus.DELETE) {
			throw new IllegalArgumentException("이미 삭제된 상품입니다.");
		}
		
		// 상품 이미지들을 삭제
		if (this.images != null) {
			this.images.forEach(image -> {
				if (image.getStatus() != ProductStatus.DELETE) {
					image.delete();
				}
			});
		}
		
		// 상품 옵션들을 삭제 (옵션 아이템들도 함께 삭제됨)
		if (this.options != null) {
			this.options.forEach(option -> {
				if (option.getStatus() != ProductStatus.DELETE) {
					option.delete();
				}
			});
		}
		
		// 상품 자체를 삭제 상태로 변경
		this.status = ProductStatus.DELETE;
		this.updatedAt = LocalDateTime.now();
	}
}
