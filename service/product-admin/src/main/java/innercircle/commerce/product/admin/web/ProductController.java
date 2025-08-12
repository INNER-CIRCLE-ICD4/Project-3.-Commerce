package innercircle.commerce.product.admin.web;

import innercircle.commerce.product.admin.application.ImageUploadUseCase;
import innercircle.commerce.product.admin.application.ProductCreateUseCase;
import innercircle.commerce.product.admin.web.dto.ApiResponse;
import innercircle.commerce.product.admin.web.dto.ImageUploadResponse;
import innercircle.commerce.product.admin.web.dto.ProductCreateRequest;
import innercircle.commerce.product.admin.web.dto.ProductCreateResponse;
import innercircle.commerce.product.core.domain.entity.Product;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

/**
 * 상품 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductController {
	private final ImageUploadUseCase imageUploadUseCase;
	private final ProductCreateUseCase productCreateUseCase;

	/**
	 * 상품을 등록합니다.
	 *
	 * @param request 상품 등록 요청
	 * @return 등록된 상품 정보
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<ProductCreateResponse>> createProduct (
			@Valid @RequestBody ProductCreateRequest request
	) {
		Product createdProduct = productCreateUseCase.create(request.toCommand());
		ProductCreateResponse response = ProductCreateResponse.from(createdProduct);

		return ResponseEntity.status(HttpStatus.CREATED)
							 .body(ApiResponse.success(response));
	}

	/**
	 * 다중 이미지를 임시 경로에 업로드
	 *
	 * @param files 업로드할 이미지 파일 목록
	 * @return 업로드된 이미지 정보 리스트
	 */
	@PostMapping("/images/temp-upload")
	public ResponseEntity<ApiResponse<List<ImageUploadResponse>>> uploadProductTempImages (
			@RequestPart List<MultipartFile> files
	) throws IOException {
		List<ImageUploadResponse> responses = imageUploadUseCase.uploadToTemp(files)
																.stream()
																.map(ImageUploadResponse::from)
																.toList();

		return ResponseEntity.ok(ApiResponse.success(responses));
	}
}