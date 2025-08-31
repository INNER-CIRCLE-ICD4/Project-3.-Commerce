package innercircle.commerce.product.core.domain;

import org.springframework.util.StringUtils;

/**
 * 임시 이미지 도메인 검증기
 */
public class TempImageValidator {

	private TempImageValidator() {}

	/**
	 * 원본 파일명을 검증합니다.
	 *
	 * @param originalName 원본 파일명
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static void validateOriginalName(String originalName) {
		if (originalName == null) {
			return;
		}

		if (originalName.trim().isEmpty()) {
			throw new IllegalArgumentException("원본 파일명은 공백일 수 없습니다");
		}
		if (originalName.length() > 255) {
			throw new IllegalArgumentException("원본 파일명은 255자를 초과할 수 없습니다");
		}
	}

	/**
	 * 이미지 URL을 검증합니다.
	 *
	 * @param url 이미지 URL
	 * @throws IllegalArgumentException 검증 실패 시
	 */
	public static void validateUrl(String url) {
		if (url == null) {
			return;
		}

		if (!StringUtils.hasText(url)) {
			throw new IllegalArgumentException("이미지 URL은 필수입니다");
		}
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			throw new IllegalArgumentException("올바른 URL 형식이 아닙니다");
		}
		if (url.length() > 2000) {
			throw new IllegalArgumentException("URL은 2000자를 초과할 수 없습니다");
		}
	}
}