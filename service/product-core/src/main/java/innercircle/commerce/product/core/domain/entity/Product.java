package innercircle.commerce.product.core.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class Product {
	private Long id;
	private String name;
	private String code;
	private Long leafCategoryId;
	private Long brandId;
	private Integer basePrice;
	private ProductStatus status;
	private SaleType saleType;
	private List<ProductOption> options;
	private List<ProductImage> images;
	private String detailContent;
	private Integer stock;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	/**
	 * 옵션이 없는 기본 상품을 생성합니다.
	 * 
	 * @param name 상품명
	 * @param leafCategoryId 최하위 카테고리 ID
	 * @param brandId 브랜드 ID
	 * @param basePrice 기본 가격
	 * @param stock 재고 수량
	 * @param images 상품 이미지 리스트
	 * @param detailContent 상품 상세 내용
	 * @return 생성된 상품 객체
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static Product create (
			String name, Long leafCategoryId, Long brandId, Integer basePrice,
			Integer stock, List<ProductImage> images, String detailContent
	) {
		validateBasicInfo(name, leafCategoryId, brandId, basePrice, stock);
		ProductImage.validateImages(images);
		validateDetailContent(detailContent);

		return Product.builder()
					  .name(name)
					  .leafCategoryId(leafCategoryId)
					  .brandId(brandId)
					  .basePrice(basePrice)
					  .stock(stock)
					  .images(images)
					  .detailContent(detailContent)
					  .saleType(SaleType.NEW)
					  .status(ProductStatus.SALE)
					  .createdAt(LocalDateTime.now())
					  .updatedAt(LocalDateTime.now())
					  .build();
	}

	/**
	 * 옵션이 있는 상품을 생성합니다.
	 * 생성 후 옵션 아이템들의 재고 상태를 확인하여 전체 상품 상태를 업데이트합니다.
	 * 
	 * @param name 상품명
	 * @param leafCategoryId 최하위 카테고리 ID
	 * @param brandId 브랜드 ID
	 * @param basePrice 기본 가격
	 * @param stock 재고 수량
	 * @param images 상품 이미지 리스트
	 * @param detailContent 상품 상세 내용
	 * @param options 상품 옵션 리스트
	 * @return 생성된 상품 객체
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static Product createWithOptions (
			String name, Long leafCategoryId, Long brandId, Integer basePrice,
			Integer stock, List<ProductImage> images, String detailContent, List<ProductOption> options
	) {
		validateBasicInfo(name, leafCategoryId, brandId, basePrice, stock);
		ProductImage.validateImages(images);
		validateDetailContent(detailContent);
		ProductOption.validateOptions(options);

		return Product.builder()
					  .name(name)
					  .leafCategoryId(leafCategoryId)
					  .brandId(brandId)
					  .basePrice(basePrice)
					  .stock(stock)
					  .images(images)
					  .detailContent(detailContent)
					  .options(options)
					  .saleType(SaleType.NEW)
					  .status(ProductStatus.SALE)
					  .createdAt(LocalDateTime.now())
					  .updatedAt(LocalDateTime.now())
					  .build();
	}

	/**
	 * 상품 기본 정보의 유효성을 검증합니다.
	 * 
	 * @param name 상품명
	 * @param leafCategoryId 최하위 카테고리 ID
	 * @param brandId 브랜드 ID
	 * @param basePrice 기본 가격
	 * @param stock 재고 수량
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	private static void validateBasicInfo (String name, Long leafCategoryId, Long brandId, Integer basePrice, Integer stock) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("상품명은 필수입니다");
		}

		if (leafCategoryId == null) {
			throw new IllegalArgumentException("최하위 카테고리 ID는 필수입니다");
		}

		if (brandId == null) {
			throw new IllegalArgumentException("브랜드 ID는 필수입니다");
		}

		if (basePrice == null || basePrice < 0) {
			throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다");
		}

		if (stock == null || stock < 0) {
			throw new IllegalArgumentException("상품 재고는 0 이상이어야 합니다");
		}
	}

	/**
	 * 상품 상세 내용의 유효성을 검증합니다.
	 * 
	 * @param detailContent 상품 상세 내용
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	private static void validateDetailContent (String detailContent) {
		if (detailContent == null || detailContent.trim().isEmpty()) {
			throw new IllegalArgumentException("상품 상세 내용은 필수입니다");
		}
	}

	/**
	 * 상품 재고를 감소시킵니다.
	 * 재고가 0 이하가 되면 상품 상태를 OUTOFSTOCK으로 변경합니다.
	 * 
	 * @param amount 감소시킬 재고 수량
	 * @throws IllegalArgumentException 감소 수량이 유효하지 않거나 재고가 부족한 경우
	 */
	public void decreaseStock (Integer amount) {
		if (amount == null || amount <= 0) {
			throw new IllegalArgumentException("감소할 재고 수량은 0보다 커야 합니다");
		}

		if (this.stock < amount) {
			throw new IllegalArgumentException("재고가 부족합니다");
		}

		this.stock -= amount;

		if (this.stock <= 0) {
			this.status = ProductStatus.OUTOFSTOCK;
		}
	}

	/**
	 * 상품 재고를 증가시킵니다.
	 * 현재 상태가 OUTOFSTOCK인 경우 SALE 상태로 변경합니다.
	 * 
	 * @param amount 증가시킬 재고 수량
	 * @throws IllegalArgumentException 증가 수량이 유효하지 않은 경우
	 */
	public void increaseStock (Integer amount) {
		if (amount == null || amount <= 0) {
			throw new IllegalArgumentException("증가할 재고 수량은 0보다 커야 합니다");
		}

		this.stock += amount;

		if (status == ProductStatus.OUTOFSTOCK) {
			this.status = ProductStatus.SALE;
		}
	}

	/**
	 * 상품 상태를 변경합니다.
	 * 
	 * @param newStatus 새로운 상품 상태
	 * @throws IllegalArgumentException 상태가 null이거나 재고와 상태가 일치하지 않는 경우
	 */
	public void changeStatus (ProductStatus newStatus) {
		if (newStatus == null) {
			throw new IllegalArgumentException("상품 상태는 필수입니다");
		}
		
		// 재고와 상태 변경 제약 조건 검증
		validateStatusChange(newStatus);
		
		this.status = newStatus;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 상태 변경 시 재고와 상태의 일관성을 검증합니다.
	 * 
	 * @param newStatus 변경하려는 상태
	 * @throws IllegalArgumentException 재고와 상태가 일치하지 않는 경우
	 */
	private void validateStatusChange(ProductStatus newStatus) {
		// 재고가 0인 상품은 SALE로 변경할 수 없음
		if (this.stock == 0 && newStatus == ProductStatus.SALE) {
			throw new IllegalArgumentException("재고가 0인 상품은 판매 상태로 변경할 수 없습니다");
		}
		
		// 재고가 있는 상품은 OUTOFSTOCK으로 변경할 수 없음
		if (this.stock > 0 && newStatus == ProductStatus.OUTOFSTOCK) {
			throw new IllegalArgumentException("재고가 있는 상품은 품절 상태로 변경할 수 없습니다");
		}
	}

	/**
	 * 판매 타입을 변경합니다.
	 * 
	 * @param newSaleType 새로운 판매 타입
	 * @throws IllegalArgumentException 판매 타입이 null인 경우
	 */
	public void changeSaleType (SaleType newSaleType) {
		if (newSaleType == null) {
			throw new IllegalArgumentException("판매 타입은 필수입니다");
		}
		this.saleType = newSaleType;
	}

	/**
	 * 상품 상세 내용을 수정합니다.
	 * 
	 * @param newDetailContent 새로운 상세 내용
	 * @throws IllegalArgumentException 상세 내용이 유효하지 않은 경우
	 */
	public void updateDetailContent (String newDetailContent) {
		validateDetailContent(newDetailContent);
		this.detailContent = newDetailContent;
	}

	/**
	 * 상품 기본 정보를 수정합니다.
	 * 
	 * @param newName 새로운 상품명
	 * @param newBasePrice 새로운 기본 가격
	 * @param newDetailContent 새로운 상세 내용
	 * @throws IllegalArgumentException 유효하지 않은 값인 경우
	 */
	public void updateBasicInfo(String newName, Integer newBasePrice, String newDetailContent) {
		if (newName == null || newName.trim().isEmpty()) {
			throw new IllegalArgumentException("상품명은 필수입니다");
		}
		
		if (newBasePrice == null || newBasePrice < 0) {
			throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다");
		}
		
		validateDetailContent(newDetailContent);
		
		this.name = newName;
		this.basePrice = newBasePrice;
		this.detailContent = newDetailContent;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 상품 이미지를 수정합니다.
	 * 
	 * @param newImages 새로운 이미지 리스트
	 * @throws IllegalArgumentException 이미지가 유효하지 않은 경우
	 */
	public void updateImages(List<ProductImage> newImages) {
		ProductImage.validateImages(newImages);
		this.images = newImages;
		this.updatedAt = LocalDateTime.now();
	}
}
