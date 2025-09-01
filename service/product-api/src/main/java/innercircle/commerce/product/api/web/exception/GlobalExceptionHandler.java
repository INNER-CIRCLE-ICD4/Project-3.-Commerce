package innercircle.commerce.product.api.web.exception;

import innercircle.commerce.product.api.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.api.application.exception.InvalidBrandException;
import innercircle.commerce.product.api.application.exception.InvalidImageFileException;
import innercircle.commerce.product.api.application.exception.ProductImageUploadInProgressException;
import innercircle.commerce.product.api.application.exception.ProductNotFoundException;
import innercircle.commerce.product.api.application.exception.StockConflictException;
import innercircle.commerce.product.api.web.dto.ApiResponse;
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
		ErrorCode errorCode = ErrorCode.DUPLICATE_PRODUCT_NAME;
		ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getMessage());
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ApiResponse.failure(errorResponse));
	}

	@ExceptionHandler(InvalidBrandException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidBrand(InvalidBrandException e) {
		log.warn("유효하지 않은 브랜드 ID: {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.INVALID_BRAND_ID;
		ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getMessage());
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ApiResponse.failure(errorResponse));
	}

	@ExceptionHandler(InvalidImageFileException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidImageFile(InvalidImageFileException e) {
		log.warn("유효하지 않은 이미지 파일: {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.INVALID_IMAGE_FILE;
		ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getMessage());
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ApiResponse.failure(errorResponse));
	}

	@ExceptionHandler(ProductImageUploadInProgressException.class)
	public ResponseEntity<ApiResponse<Void>> handleImageUploadInProgress(ProductImageUploadInProgressException e) {
		log.warn("이미지 업로드 진행 중: {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.IMAGE_UPLOAD_IN_PROGRESS;
		ErrorResponse errorResponse = ErrorResponse.of(errorCode);
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ApiResponse.failure(errorResponse));
	}
	
	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleProductNotFound(ProductNotFoundException e) {
		log.warn("상품을 찾을 수 없음: {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.PRODUCT_NOT_FOUND;
		ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getMessage());
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ApiResponse.failure(errorResponse));
	}

	@ExceptionHandler(StockConflictException.class)
	public ResponseEntity<ApiResponse<Void>> handleStockConflict(StockConflictException e) {
		log.warn("재고 변경 중 충돌 발생: {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.STOCK_CONFLICT;
		ErrorResponse errorResponse = ErrorResponse.of(errorCode);
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ApiResponse.failure(errorResponse));
	}

	// === Validation 예외 처리 ===
	
	@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
	public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception e) {
		String fieldError = extractValidationErrorMessage(e);
		log.warn("Validation 오류: {}", fieldError);
		
		ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
		ErrorResponse errorResponse = ErrorResponse.of(errorCode, fieldError);
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ApiResponse.failure(errorResponse));
	}

	// === Domain 계층 예외 처리 ===
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
		log.warn("도메인 규칙 위반: {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.DOMAIN_RULE_VIOLATION;
		ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getMessage());
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ApiResponse.failure(errorResponse));
	}

	// === 시스템 예외 처리 ===
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
		log.error("예상치 못한 서버 오류", e);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		ErrorResponse errorResponse = ErrorResponse.of(errorCode);
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ApiResponse.failure(errorResponse));
	}

	// === Private Methods ===
	
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