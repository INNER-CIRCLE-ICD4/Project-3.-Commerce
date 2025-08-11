package innercircle.commerce.product.admin.web;

import innercircle.commerce.product.admin.application.ImageUploadUseCase;
import innercircle.commerce.product.admin.web.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 이미지 업로드 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/products/images")
@RequiredArgsConstructor
@Validated
public class ProductController {
	private final ImageUploadUseCase imageUploadUseCase;


	/**
	 * 다중 이미지를 임시 경로에 업로드
	 *
	 * @param files 업로드할 이미지 파일 목록
	 * @return 업로드된 이미지 정보 리스트
	 */
	@PostMapping("/temp-upload")
	public ResponseEntity<List<ImageUploadResponse>> uploadProductTempImages (
			@RequestPart List<MultipartFile> files
	) throws IOException {
		List<ImageUploadResponse> responses = imageUploadUseCase.uploadToTemp(files)
																.stream()
																.map(ImageUploadResponse::from)
																.toList();

		return ResponseEntity.ok(responses);
	}
}