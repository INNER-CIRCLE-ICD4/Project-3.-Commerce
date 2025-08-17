package innercircle.commerce.product.admin.web.exception;

public record ErrorResponse(
		String code,
		String message
) {
	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(
				errorCode.getCode(),
				errorCode.getMessage()
		);
	}
	
	public static ErrorResponse of(ErrorCode errorCode, Object... args) {
		return new ErrorResponse(
				errorCode.getCode(),
				String.format(errorCode.getMessage(), args)
		);
	}
	
	public static ErrorResponse of(String code, String message) {
		return new ErrorResponse(code, message);
	}
}