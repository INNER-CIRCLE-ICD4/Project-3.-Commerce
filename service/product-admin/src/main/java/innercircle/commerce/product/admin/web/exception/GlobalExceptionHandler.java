package innercircle.commerce.product.admin.web.exception;

import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.InvalidBrandException;
import innercircle.commerce.product.admin.application.exception.InvalidCategoryException;
import innercircle.commerce.product.admin.application.exception.InvalidImageFileException;
import innercircle.commerce.product.admin.application.exception.NotFoundTempImageException;
import innercircle.commerce.product.admin.application.exception.ProductImageUploadInProgressException;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.admin.application.exception.StockConflictException;
import innercircle.commerce.product.admin.web.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// === Application 계층 예외 처리 ===

	@ExceptionHandler(DuplicateProductNameException.class)
	public ResponseEntity<ApiResponse<Void>> handleDuplicateProductName(DuplicateProductNameException e) {
		log.warn("상품명 중복 오류: {}", e.getMessage());
		return buildErrorResponseEntity(ErrorCode.DUPLICATE_PRODUCT_NAME, e.getMessage());
	}

	@ExceptionHandler(InvalidBrandException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidBrand(InvalidBrandException e) {
		log.warn("유효하지 않은 브랜드 ID: {}", e.getMessage());
		return buildErrorResponseEntity(ErrorCode.INVALID_BRAND_ID, e.getMessage());
	}

	@ExceptionHandler(InvalidCategoryException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidCategory(InvalidCategoryException e) {
		log.warn("유효하지 않은 카테고리 ID: {}", e.getMessage());
		return buildErrorResponseEntity(ErrorCode.INVALID_CATEGORY_ID, e.getMessage());
	}

	@ExceptionHandler(NotFoundTempImageException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotFoundTempImage(NotFoundTempImageException e) {
		log.warn("임시 이미지 찾을 수 없음: {}", e.getMessage());
		return buildErrorResponseEntity(ErrorCode.NOT_FOUND_TEMP_IMAGE, e.getMessage());
	}

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleProductNotFound(ProductNotFoundException e) {
		log.warn("상품을 찾을 수 없음: {}", e.getMessage());
		return buildErrorResponseEntity(ErrorCode.PRODUCT_NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler(InvalidImageFileException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidImageFile(InvalidImageFileException e) {
		log.warn("유효하지 않은 이미지 파일: {}", e.getMessage());
		return buildErrorResponseEntity(ErrorCode.INVALID_IMAGE_FILE, e.getMessage());
	}

	@ExceptionHandler(ProductImageUploadInProgressException.class)
	public ResponseEntity<ApiResponse<Void>> handleImageUploadInProgress(ProductImageUploadInProgressException e) {
		log.warn("이미지 업로드 진행 중: {}", e.getMessage());
		return buildErrorResponseEntity(ErrorCode.IMAGE_UPLOAD_IN_PROGRESS);
	}

	@ExceptionHandler(StockConflictException.class)
	public ResponseEntity<ApiResponse<Void>> handleStockConflict(StockConflictException e) {
		log.warn("재고 변경 중 충돌 발생: {}", e.getMessage());
		return buildErrorResponseEntity(ErrorCode.STOCK_CONFLICT);
	}

	// === Validation 예외 처리 ===

	@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
	public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception e) {
		String fieldError = extractValidationErrorMessage(e);
		log.warn("Validation 오류: {}", fieldError);
		return buildErrorResponseEntity(ErrorCode.VALIDATION_ERROR, fieldError);
	}

	// === Domain 계층 예외 처리 ===

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
		log.warn("도메인 규칙 위반: {}", e.getMessage());
		return buildErrorResponseEntity(ErrorCode.DOMAIN_RULE_VIOLATION, e.getMessage());
	}

	// === 시스템 예외 처리 ===

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
		log.error("예상치 못한 서버 오류", e);
		return buildErrorResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	// === Private Methods ===

	private ResponseEntity<ApiResponse<Void>> buildErrorResponseEntity(ErrorCode errorCode) {
		ErrorResponse errorResponse = ErrorResponse.of(errorCode);
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ApiResponse.failure(errorResponse));
	}

	private ResponseEntity<ApiResponse<Void>> buildErrorResponseEntity(ErrorCode errorCode, String message) {
		ErrorResponse errorResponse = ErrorResponse.of(errorCode, message);
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ApiResponse.failure(errorResponse));
	}

	private String extractValidationErrorMessage(Exception e) {
		String defaultMessage = "입력값이 유효하지 않습니다";

		if (e instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
			return methodArgumentNotValidException.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(fieldError -> fieldError.getDefaultMessage())
				.orElse(defaultMessage);
		} else if (e instanceof BindException bindException) {
			return bindException.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(fieldError -> fieldError.getDefaultMessage())
				.orElse(defaultMessage);
		}

		return defaultMessage;
	}
}