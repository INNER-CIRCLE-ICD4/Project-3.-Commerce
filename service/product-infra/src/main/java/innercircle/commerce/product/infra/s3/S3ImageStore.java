package innercircle.commerce.product.infra.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * S3를 이용한 이미지 업로드/삭제 저장소
 */
@Component
public class S3ImageStore {

	private final AmazonS3Client amazonS3Client;
	private final String bucketName;
	private final String baseUrl;

	public S3ImageStore (
			AmazonS3Client amazonS3Client,
			@Value("${cloud.aws.s3.bucket}") String bucketName,
			@Value("${cloud.aws.s3.base-url}") String baseUrl
	) {
		this.amazonS3Client = amazonS3Client;
		this.bucketName = bucketName;
		this.baseUrl = baseUrl;
	}

	/**
	 * 지정된 S3 키로 이미지를 업로드합니다.
	 *
	 * @param file  업로드할 파일
	 * @param s3Key S3에 저장할 키 (경로 포함)
	 * @return 업로드된 이미지 정보
	 * @throws IOException 파일 처리 중 오류 발생 시
	 */
	public String upload (MultipartFile file, String s3Key) throws IOException {
		String originalName = file.getOriginalFilename();

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());
		metadata.addUserMetadata("original-name", originalName);

		PutObjectRequest request = new PutObjectRequest(
				bucketName, s3Key, file.getInputStream(), metadata);

		amazonS3Client.putObject(request);

		return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, amazonS3Client.getRegionName(), s3Key);
	}

	/**
	 * S3 객체를 다른 위치로 이동합니다.
	 *
	 * @param sourceKey 원본 S3 키
	 * @param targetKey 대상 S3 키
	 * @return 이동된 파일의 URL (Optional.empty()면 원본 파일을 찾지 못함)
	 */
	public Optional<String> move (String sourceKey, String targetKey) {
		if (!amazonS3Client.doesObjectExist(bucketName, sourceKey)) {
			return Optional.empty();
		}

		// 복사
		amazonS3Client.copyObject(bucketName, sourceKey, bucketName, targetKey);

		// 원본 삭제
		amazonS3Client.deleteObject(bucketName, sourceKey);

		return Optional.of(baseUrl + "/" + targetKey);
	}

	/**
	 * 지정된 키의 객체가 존재하는지 확인합니다.
	 *
	 * @param s3Key 확인할 S3 키
	 * @return 존재 여부
	 */
	public boolean exists (String s3Key) {
		return amazonS3Client.doesObjectExist(bucketName, s3Key);
	}

	/**
	 * 지정된 키들의 객체를 일괄 삭제합니다.
	 *
	 * @param s3Keys 삭제할 S3 키 목록
	 */
	public void delete (List<String> s3Keys) {
		for (String s3Key : s3Keys) {
			if (amazonS3Client.doesObjectExist(bucketName, s3Key)) {
				amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, s3Key));
			}
		}
	}

	/**
	 * 단일 객체를 삭제합니다.
	 *
	 * @param s3Key 삭제할 S3 키
	 */
	public void delete (String s3Key) {
		if (amazonS3Client.doesObjectExist(bucketName, s3Key)) {
			amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, s3Key));
		}
	}

	/**
	 * 업로드된 이미지 정보
	 */
	public record UploadedImageInfo(
			String originalName,
			String url,
			String s3Key,
			long size,
			String contentType
	) {
	}
}