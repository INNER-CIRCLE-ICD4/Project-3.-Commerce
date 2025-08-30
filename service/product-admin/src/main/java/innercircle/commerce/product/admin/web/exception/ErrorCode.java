package innercircle.commerce.product.admin.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	
	// === 400 Bad Request (도메인_001~099) ===
	
	// Validation 오류
	VALIDATION_ERROR("VALIDATION_001", "입력값이 유효하지 않습니다: %s", HttpStatus.BAD_REQUEST),
	MISSING_REQUIRED_FIELD("VALIDATION_002", "필수 입력값이 누락되었습니다: %s", HttpStatus.BAD_REQUEST),
	INVALID_FORMAT("VALIDATION_003", "입력값 형식이 올바르지 않습니다: %s", HttpStatus.BAD_REQUEST),
	
	// 도메인 규칙 위반
	DOMAIN_RULE_VIOLATION("DOMAIN_001", "도메인 규칙 위반: %s", HttpStatus.BAD_REQUEST),
	BUSINESS_RULE_VIOLATION("DOMAIN_002", "비즈니스 규칙 위반: %s", HttpStatus.BAD_REQUEST),
	
	// 상품 관련 Bad Request
	INVALID_BRAND_ID("PRODUCT_001", "유효하지 않은 브랜드 ID입니다: %s", HttpStatus.BAD_REQUEST),
	INVALID_CATEGORY_ID("PRODUCT_002", "유효하지 않은 카테고리 ID입니다: %s", HttpStatus.BAD_REQUEST),
	INVALID_IMAGE_FILE("PRODUCT_003", "유효하지 않은 이미지 파일입니다: %s", HttpStatus.BAD_REQUEST),
	
	// === 404 Not Found (도메인_401~499) ===
	PRODUCT_NOT_FOUND("PRODUCT_401", "상품을 찾을 수 없습니다: %s", HttpStatus.NOT_FOUND),
	NOT_FOUND_TEMP_IMAGE("PRODUCT_402", "임시 이미지를 찾을 수 없습니다: %s", HttpStatus.NOT_FOUND),
	NOT_FOUND_IMAGE("PRODUCT_403", "이미지를 찾을 수 없습니다: %s", HttpStatus.NOT_FOUND),
	
	// === 409 Conflict (도메인_901~999) ===
	DUPLICATE_PRODUCT_NAME("PRODUCT_901", "상품명이 중복되었습니다: %s", HttpStatus.CONFLICT),
	IMAGE_UPLOAD_IN_PROGRESS("PRODUCT_902", "이미지 업로드가 진행 중입니다", HttpStatus.CONFLICT),
	STOCK_CONFLICT("PRODUCT_903", "재고 변경 중 충돌이 발생했습니다. 잠시 후 다시 시도해주세요", HttpStatus.CONFLICT),
	
	// === 500 Internal Server Error (도메인_501~599) ===
	INTERNAL_SERVER_ERROR("SYSTEM_501", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
	DATABASE_ERROR("SYSTEM_502", "데이터베이스 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
	
	// === 502 Bad Gateway (도메인_521~599) ===
	EXTERNAL_API_ERROR("SYSTEM_521", "외부 API 호출 중 오류가 발생했습니다", HttpStatus.BAD_GATEWAY),
	S3_UPLOAD_ERROR("PRODUCT_521", "이미지 업로드 중 오류가 발생했습니다", HttpStatus.BAD_GATEWAY);
	
	private final String code;
	private final String message;  
	private final HttpStatus httpStatus;
}