package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static innercircle.commerce.product.core.domain.TempImageValidator.*;

/**
 * 임시 이미지 도메인 모델
 * 상품 등록 전에 업로드된 임시 이미지를 나타냅니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TempImage {
	private Long id;
	private String originalName;
	private String url;

	private TempImage(Long id, String originalName, String url) {
		this.id = id == null ? IdGenerator.generateId() : id;
		setOriginalName(originalName);
		setUrl(url);
	}

	/**
	 * 임시 이미지를 생성합니다.
	 * ID는 Snowflake를 통해 자동 생성됩니다.
	 *
	 * @param originalName 원본 파일명
	 * @param url          업로드된 이미지 URL
	 * @return 생성된 임시 이미지 객체
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static TempImage create(String originalName, String url) {
		return new TempImage(null, originalName, url);
	}

	/**
	 * URL 없이 임시 이미지를 생성합니다.
	 * 업로드 전에 ID를 미리 생성할 때 사용됩니다.
	 *
	 * @param originalName 원본 파일명
	 * @return 생성된 임시 이미지 객체 (URL은 null)
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static TempImage createWithoutUrl(String originalName) {
		TempImage tempImage = new TempImage();
		tempImage.id = IdGenerator.generateId();
		tempImage.setOriginalName(originalName);
		tempImage.url = null;
		return tempImage;
	}

	/**
	 * 기존 임시 이미지를 복원합니다.
	 * 주로 데이터베이스에서 조회한 데이터로 객체를 복원할 때 사용됩니다.
	 *
	 * @param id           임시 이미지 ID
	 * @param originalName 원본 파일명
	 * @param url          업로드된 이미지 URL
	 * @return 복원된 임시 이미지 객체
	 */
	public static TempImage restore(Long id, String originalName, String url) {
		return new TempImage(id, originalName, url);
	}

	private void setOriginalName(String originalName) {
		validateOriginalName(originalName);
		this.originalName = originalName;
	}

	private void setUrl(String url) {
		validateUrl(url);
		this.url = url;
	}
}