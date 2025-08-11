package innercircle.commerce.product.admin.application;

import innercircle.commerce.common.snowflake.Snowflake;
import innercircle.commerce.product.admin.application.dto.ImageUploadInfo;
import innercircle.commerce.product.admin.application.validator.ImageValidator;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 이미지 임시 업로드 UseCase
 */
@Service
public class ImageUploadUseCase {

	private final S3ImageStore s3ImageStore;
	private final ImageValidator imageValidator;

	public ImageUploadUseCase (S3ImageStore s3ImageStore, ImageValidator imageValidator) {
		this.s3ImageStore = s3ImageStore;
		this.imageValidator = imageValidator;
	}

	/**
	 * 이미지들을 임시 경로에 업로드합니다.
	 *
	 * @param files 이미지 업로드 명령 리스트
	 * @return 업로드된 이미지 정보
	 * @throws IOException 업로드 실패 시
	 */
	public List<ImageUploadInfo> uploadToTemp (List<MultipartFile> files) throws IOException {
		for (MultipartFile file : files) {
			imageValidator.validate(file);
		}

		List<ImageUploadInfo> uploadInfos = new ArrayList<>();
		Snowflake snowflake = new Snowflake();

		try {
			for (MultipartFile file : files) {
				String originalFilename = file.getOriginalFilename();
				Long tempId = snowflake.nextId();
				String s3Key = buildTempPath(tempId.toString(), getFileExtension(originalFilename));

				String uploadUrl = s3ImageStore.upload(file, s3Key);
				uploadInfos.add(new ImageUploadInfo(tempId, originalFilename, uploadUrl));
			}

			return uploadInfos;

		} catch (Exception e) {
			if (!uploadInfos.isEmpty()) {
				List<String> tempKeys = uploadInfos.stream()
												   .map(ImageUploadInfo::url)
												   .toList();
				s3ImageStore.delete(tempKeys);
			}
			throw e;
		}
	}

	/**
	 * 임시 경로 생성
	 */
	private String buildTempPath (String tempId, String extension) {
		return String.format("commerce/temp/images/%s/original.%s", tempId, extension);
	}

	/**
	 * 파일명에서 확장자 추출
	 */
	private String getFileExtension (String filename) {
		if (filename == null) return "jpg";
		int lastDotIndex = filename.lastIndexOf('.');
		if (lastDotIndex == -1) return "jpg";
		return filename.substring(lastDotIndex + 1).toLowerCase();
	}
}