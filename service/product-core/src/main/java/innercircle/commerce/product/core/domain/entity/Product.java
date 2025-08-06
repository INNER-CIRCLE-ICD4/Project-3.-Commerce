package innercircle.commerce.product.core.domain.entity;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

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
	 * 상품을 생성합니다.
	 * 옵션이 없는 경우 빈 리스트로 처리됩니다.
	 *
	 * @param name           상품명
	 * @param leafCategoryId 최하위 카테고리 ID
	 * @param brandId        브랜드 ID
	 * @param basePrice      기본 가격
	 * @param stock          재고 수량
	 * @param options        상품 옵션 리스트 (nullable)
	 * @param images         상품 이미지 리스트
	 * @param detailContent  상품 상세 내용
	 * @return 생성된 상품 객체
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static Product create (
			String name, Long leafCategoryId, Long brandId, Integer basePrice,
			Integer stock, List<ProductOption> options, List<ProductImage> images, String detailContent
	) {
		validateBasicInfo(name, leafCategoryId, brandId, basePrice, stock);
		validateDetailContent(detailContent);
		ProductImage.validateImages(images);

		List<ProductOption> validatedOptions = validateAndProcessOptions(options);

		return Product.builder()
					  .name(name)
					  .leafCategoryId(leafCategoryId)
					  .brandId(brandId)
					  .basePrice(basePrice)
					  .stock(stock)
					  .options(validatedOptions)
					  .images(images)
					  .detailContent(detailContent)
					  .saleType(SaleType.NEW)
					  .status(determineInitialStatus(stock))
					  .createdAt(LocalDateTime.now())
					  .updatedAt(LocalDateTime.now())
					  .build();
	}

	/**
	 * 상품 옵션을 검증하고 처리합니다.
	 * 옵션이 null이거나 비어있는 경우 빈 리스트를 반환합니다.
	 *
	 * @param options 상품 옵션 리스트 (nullable)
	 * @return 검증된 옵션 리스트 (null인 경우 빈 리스트)
	 * @throws IllegalArgumentException 옵션 검증 실패 시
	 */
	private static List<ProductOption> validateAndProcessOptions (List<ProductOption> options) {
		if (CollectionUtils.isEmpty(options)) {
			return Collections.emptyList();
		}

		ProductOption.validateOptions(options);
		return options;
	}

	/**
	 * 재고 수량에 따른 초기 상품 상태를 결정합니다.
	 * 재고가 0보다 크면 SALE, 0 이하면 OUTOFSTOCK 상태를 반환합니다.
	 *
	 * @param stock 재고 수량
	 * @return 재고에 따른 초기 상태 (SALE 또는 OUTOFSTOCK)
	 */
	private static ProductStatus determineInitialStatus (Integer stock) {
		return stock > 0 ? ProductStatus.SALE : ProductStatus.OUTOFSTOCK;
	}

	/**
	 * 상품 기본 정보의 유효성을 검증합니다.
	 *
	 * @param name           상품명
	 * @param leafCategoryId 최하위 카테고리 ID
	 * @param brandId        브랜드 ID
	 * @param basePrice      기본 가격
	 * @param stock          재고 수량
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	private static void validateBasicInfo (String name, Long leafCategoryId, Long brandId, Integer basePrice, Integer stock) {
		if (StringUtils.isBlank(name)) {
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
		if (StringUtils.isBlank(detailContent)) {
			throw new IllegalArgumentException("상품 상세 내용은 필수입니다");
		}
	}

	/**
	 * 재고 감소가 가능한지 검증합니다.
	 * 감소할 수량이 유효하고 현재 재고가 충분한지 확인합니다.
	 *
	 * @param quantity 감소시킬 재고 수량
	 * @return 재고 감소 가능 여부
	 */
	public boolean canDecreaseStock(int quantity) {
		if (quantity <= 0) {
			return false;
		}
		return this.stock >= quantity;
	}

	/**
	 * 재고 증가가 가능한지 검증합니다.
	 * 증가할 수량이 유효한지 확인합니다.
	 *
	 * @param quantity 증가시킬 재고 수량
	 * @return 재고 증가 가능 여부
	 */
	public boolean canIncreaseStock(int quantity) {
		return quantity > 0;
	}

	/**
	 * 재고 수량에 따른 적절한 상태를 결정합니다.
	 * 서비스 계층에서 재고 업데이트 후 상태 동기화에 사용됩니다.
	 *
	 * @param newStock 새로운 재고 수량
	 * @return 재고에 맞는 상품 상태
	 */
	public ProductStatus determineStatusByStock(int newStock) {
		if (newStock <= 0) {
			return ProductStatus.OUTOFSTOCK;
		} else if (this.status == ProductStatus.OUTOFSTOCK) {
			return ProductStatus.SALE;
		}
		return this.status;
	}

	/**
	 * 상품을 판매 상태로 변경합니다.
	 * 재고가 0인 경우 변경할 수 없습니다.
	 *
	 * @throws IllegalArgumentException 재고가 0 이하인 경우
	 */
	public void changeToSale () {
		if (this.stock <= 0) {
			throw new IllegalArgumentException("재고가 0인 상품은 판매 상태로 변경할 수 없습니다");
		}
		this.status = ProductStatus.SALE;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 상품을 품절 상태로 변경합니다.
	 * 재고가 있는 경우 변경할 수 없습니다.
	 *
	 * @throws IllegalArgumentException 재고가 0보다 큰 경우
	 */
	public void changeToOutOfStock () {
		if (this.stock > 0) {
			throw new IllegalArgumentException("재고가 있는 상품은 품절 상태로 변경할 수 없습니다");
		}
		this.status = ProductStatus.OUTOFSTOCK;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 상품을 판매 중지 상태로 변경합니다.
	 * 재고와 상관없이 변경할 수 있습니다.
	 * 관리자가 수동으로 상품 판매를 중지할 때 사용됩니다.
	 */
	public void changeToClose () {
		this.status = ProductStatus.CLOSE;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 판매 타입을 신상품으로 변경합니다.
	 * 새로 출시된 상품이거나 다시 신상품으로 마케팅하고자 할 때 사용합니다.
	 */
	public void changeToNew () {
		this.saleType = SaleType.NEW;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 판매 타입을 기존 상품으로 변경합니다.
	 * 신상품 기간이 지났거나 일반 상품으로 분류하고자 할 때 사용합니다.
	 */
	public void changeToOld () {
		this.saleType = SaleType.OLD;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 상품 상세 내용을 수정합니다.
	 * HTML 형식의 상세 설명을 업데이트하며, 빈 값이나 null은 허용되지 않습니다.
	 *
	 * @param newDetailContent 새로운 상세 내용 (HTML 형식, 필수)
	 * @throws IllegalArgumentException 상세 내용이 null이거나 빈 값인 경우
	 */
	public void updateDetailContent (String newDetailContent) {
		validateDetailContent(newDetailContent);
		this.detailContent = newDetailContent;
	}

	/**
	 * 상품 기본 정보를 일괄 수정합니다.
	 * 상품명, 가격, 상세 내용을 한 번에 업데이트하며, 모든 값은 유효성 검증을 거칩니다.
	 *
	 * @param newName          새로운 상품명 (필수, 공백 불가)
	 * @param newBasePrice     새로운 기본 가격 (필수, 0 이상)
	 * @param newDetailContent 새로운 상세 내용 (HTML 형식, 필수)
	 * @throws IllegalArgumentException 상품명이 null이거나 빈 값인 경우
	 * @throws IllegalArgumentException 가격이 null이거나 음수인 경우
	 * @throws IllegalArgumentException 상세 내용이 null이거나 빈 값인 경우
	 */
	public void updateBasicInfo (String newName, Integer newBasePrice, String newDetailContent) {
		if (StringUtils.isBlank(newName)) {
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
	 * 기존 이미지를 모두 교체하며, 최소 1개 이상의 이미지와 대표 이미지 1개가 필요합니다.
	 *
	 * @param newImages 새로운 이미지 리스트 (최소 1개, 대표 이미지 1개 필수)
	 * @throws IllegalArgumentException 이미지 리스트가 null이거나 비어있는 경우
	 * @throws IllegalArgumentException 대표 이미지가 없거나 2개 이상인 경우
	 */
	public void updateImages (List<ProductImage> newImages) {
		ProductImage.validateImages(newImages);
		this.images = newImages;
		this.updatedAt = LocalDateTime.now();
	}
}
