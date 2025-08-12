package innercircle.commerce.product.admin.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private final boolean success;
	private final T data;
	private final ErrorResponse error;
	private final LocalDateTime timestamp;

	private ApiResponse(boolean success, T data, ErrorResponse error, LocalDateTime timestamp) {
		this.success = success;
		this.data = data;
		this.error = error;
		this.timestamp = timestamp;
	}

	/**
	 * 성공 응답 생성
	 */
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(
				true,
				data,
				null,
				LocalDateTime.now()
		);
	}

	/**
	 * 데이터 없는 성공 응답 생성
	 */
	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>(
				true,
				null,
				null,
				LocalDateTime.now()
		);
	}

	/**
	 * 실패 응답 생성
	 */
	public static <T> ApiResponse<T> failure(ErrorResponse error) {
		return new ApiResponse<>(
				false,
				null,
				error,
				LocalDateTime.now()
		);
	}
}