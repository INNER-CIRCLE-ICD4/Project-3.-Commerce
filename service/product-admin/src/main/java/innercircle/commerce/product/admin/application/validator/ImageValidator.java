package innercircle.commerce.product.admin.application.validator;

import innercircle.commerce.product.admin.application.exception.InvalidImageFileException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * 이미지 파일 검증을 담당하는 클래스
 */
@Component
public class ImageValidator {

	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"image/jpeg", "image/png", "image/webp"
	);

	// JPEG, PNG, WebP 파일 시그니처
	private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
	private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47};
	private static final byte[] WEBP_SIGNATURE = {0x52, 0x49, 0x46, 0x46}; // RIFF

	@Value("${app.image.max-file-size}") // 기본값 5MB
	private long maxFileSize;

	/**
	 * 이미지 파일의 유효성을 검증합니다.
	 *
	 * @param file 검증할 파일
	 * @throws InvalidImageFileException 검증 실패 시
	 */
	public void validate (MultipartFile file) {
		String originalFilename = file.getOriginalFilename();

		validateFilename(originalFilename);
		validateFileExists(file, originalFilename);
		validateFileExtension(originalFilename);
		validateFileSize(file, originalFilename);
		validateContentType(file, originalFilename);
		validateFileSignature(file, originalFilename);
	}

	/**
	 * 파일명 존재 여부 및 확장자 존재 여부 확인
	 */
	private void validateFilename (String filename) {
		if (StringUtils.isBlank(filename)) {
			throw new InvalidImageFileException(filename, "MISSING_FILENAME");
		}

		// 확장자가 없는 경우도 파일명 오류로 처리
		String extension = getFileExtension(filename);
		if (extension.isEmpty()) {
			throw new InvalidImageFileException(filename, "MISSING_FILENAME");
		}
	}

	/**
	 * 파일이 존재하는지 확인
	 */
	private void validateFileExists (MultipartFile file, String filename) {
		if (file.isEmpty()) {
			throw new InvalidImageFileException(filename, "EMPTY_FILE");
		}
	}

	/**
	 * 파일 확장자 검증
	 */
	private void validateFileExtension (String filename) {
		String extension = getFileExtension(filename).toLowerCase();

		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new InvalidImageFileException(filename, "INVALID_EXTENSION");
		}
	}

	/**
	 * 파일 크기 검증
	 */
	private void validateFileSize (MultipartFile file, String filename) {
		if (file.getSize() > maxFileSize) {
			throw new InvalidImageFileException(filename, "FILE_TOO_LARGE");
		}
	}

	/**
	 * Content-Type 검증
	 */
	private void validateContentType (MultipartFile file, String filename) {
		String contentType = file.getContentType();

		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new InvalidImageFileException(filename, "INVALID_CONTENT_TYPE");
		}
	}

	/**
	 * 파일 시그니처 검증 (악성 파일 방지)
	 */
	private void validateFileSignature (MultipartFile file, String filename) {
		try {
			byte[] fileBytes = file.getBytes();

			if (fileBytes.length < 4) {
				throw new InvalidImageFileException(filename, "INVALID_FILE_SIGNATURE");
			}

			if (!isValidImageSignature(fileBytes)) {
				throw new InvalidImageFileException(filename, "INVALID_FILE_SIGNATURE");
			}
		} catch (IOException e) {
			throw new InvalidImageFileException(filename, "FILE_READ_ERROR");
		}
	}

	/**
	 * 유효한 이미지 파일 시그니처인지 확인
	 */
	private boolean isValidImageSignature (byte[] fileBytes) {
		return isJpegSignature(fileBytes) ||
				isPngSignature(fileBytes) ||
				isWebpSignature(fileBytes);
	}

	private boolean isJpegSignature (byte[] fileBytes) {
		if (fileBytes.length < JPEG_SIGNATURE.length) return false;

		for (int i = 0; i < JPEG_SIGNATURE.length; i++) {
			if (fileBytes[i] != JPEG_SIGNATURE[i]) return false;
		}

		return true;
	}

	private boolean isPngSignature (byte[] fileBytes) {
		if (fileBytes.length < PNG_SIGNATURE.length) return false;

		for (int i = 0; i < PNG_SIGNATURE.length; i++) {
			if (fileBytes[i] != PNG_SIGNATURE[i]) return false;
		}

		return true;
	}

	private boolean isWebpSignature (byte[] fileBytes) {
		if (fileBytes.length < 12) return false;

		// RIFF 체크
		for (int i = 0; i < WEBP_SIGNATURE.length; i++) {
			if (fileBytes[i] != WEBP_SIGNATURE[i]) return false;
		}

		// WEBP 체크 (8번째부터 4바이트)
		return fileBytes[8] == 0x57 && fileBytes[9] == 0x45 &&
				fileBytes[10] == 0x42 && fileBytes[11] == 0x50;
	}

	/**
	 * 파일명에서 확장자 추출
	 */
	private String getFileExtension (String filename) {
		if (filename == null) {
			return "";
		}
		int lastDotIndex = filename.lastIndexOf('.');
		if (lastDotIndex == -1) {
			return "";
		}
		return filename.substring(lastDotIndex + 1);
	}
}