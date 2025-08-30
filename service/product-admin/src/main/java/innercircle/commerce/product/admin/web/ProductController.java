package innercircle.commerce.product.admin.web;

import innercircle.commerce.product.admin.application.ImageUploadUseCase;
import innercircle.commerce.product.admin.application.ProductCreateUseCase;
import innercircle.commerce.product.admin.application.ProductDeleteUseCase;
import innercircle.commerce.product.admin.application.ProductRetrieveUseCase;
import innercircle.commerce.product.admin.application.ProductUpdateUseCase;
import innercircle.commerce.product.admin.application.dto.ProductAdminInfo;
import innercircle.commerce.product.admin.application.dto.ProductListAdminInfo;
import innercircle.commerce.product.admin.application.dto.ProductListQuery;
import innercircle.commerce.product.admin.application.dto.ProductUpdateCommand;
import innercircle.commerce.product.admin.web.dto.ApiResponse;
import innercircle.commerce.product.admin.web.dto.ImageUploadResponse;
import innercircle.commerce.product.admin.web.dto.ProductCreateRequest;
import innercircle.commerce.product.admin.web.dto.ProductCreateResponse;
import innercircle.commerce.product.admin.web.dto.ProductDetailResponse;
import innercircle.commerce.product.admin.web.dto.ProductListResponse;
import innercircle.commerce.product.admin.web.dto.ProductUpdateRequest;
import innercircle.commerce.product.admin.web.dto.ProductUpdateResponse;
import innercircle.commerce.product.core.domain.Product;
import innercircle.commerce.product.core.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

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
	private final ProductUpdateUseCase productUpdateUseCase;
	private final ProductDeleteUseCase productDeleteUseCase;
	private final ProductRetrieveUseCase productRetrieveUseCase;

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

	/**
	 * 상품 정보를 수정합니다. (기본 정보 + 이미지 변경사항)
	 *
	 * @param id 상품 ID
	 * @param request 상품 수정 요청
	 * @return 수정된 상품 정보
	 */
	@PatchMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductUpdateResponse>> updateProduct(
			@PathVariable Long id,
			@Valid @RequestBody ProductUpdateRequest request
	) {
		var command = request.toCommand(id);
		
		Product updatedProduct = productUpdateUseCase.updateProduct(command);
		ProductUpdateResponse response = ProductUpdateResponse.from(updatedProduct);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 상품을 삭제합니다.
	 *
	 * @param id 삭제할 상품 ID
	 * @return 삭제 완료 응답
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
		productDeleteUseCase.deleteProduct(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 상품 목록을 조회합니다.
	 *
	 * @param status 상품 상태 (선택사항)
	 * @param categoryId 카테고리 ID (선택사항)
	 * @param pageable 페이징 정보
	 * @return 상품 목록
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getProducts(
			@RequestParam(required = false) ProductStatus status,
			@RequestParam(required = false) Long categoryId,
			@PageableDefault(size = 20) Pageable pageable
	) {
		ProductListQuery query = ProductListQuery.builder()
				.status(status)
				.categoryId(categoryId)
				.pageable(pageable)
				.build();

		Page<ProductListAdminInfo> productInfos = productRetrieveUseCase.getProducts(query);
		Page<ProductListResponse> responses = productInfos.map(ProductListResponse::from);

		return ResponseEntity.ok(ApiResponse.success(responses));
	}

	/**
	 * 상품 상세 정보를 조회합니다.
	 *
	 * @param id 상품 ID
	 * @return 상품 상세 정보
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Long id) {
		ProductAdminInfo productInfo = productRetrieveUseCase.getProduct(id);
		ProductDetailResponse response = ProductDetailResponse.from(productInfo);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}