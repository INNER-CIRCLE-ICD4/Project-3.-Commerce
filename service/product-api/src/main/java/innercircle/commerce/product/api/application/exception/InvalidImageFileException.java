package innercircle.commerce.product.api.application.exception;

import lombok.Getter;

/**
 * 유효하지 않은 이미지 파일에 대한 예외
 */
@Getter
public class InvalidImageFileException extends RuntimeException {

	private final String fileName;
	private final String reason;

	public InvalidImageFileException (String fileName, String reason) {
		this.fileName = fileName;
		this.reason = reason;
	}
}
