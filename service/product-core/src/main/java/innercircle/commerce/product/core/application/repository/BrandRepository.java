package innercircle.commerce.product.admin.application.repository;


public interface BrandRepository {
	/**
	 * 브랜드 ID로 브랜드 존재 여부를 확인합니다.
	 *
	 * @param brandId 브랜드 ID
	 * @return 존재 여부
	 */
	boolean existsById (Long brandId);
}
