package innercircle.commerce.product.api.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import innercircle.commerce.product.api.web.exception.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 응답을 위한 공통 래퍼 클래스
 *
 * @param <T> 응답 데이터 타입
 * @author 황인웅
 * @version 1.0.0
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private final boolean success;
	private final T data;
	private final ErrorResponse error;
	private final LocalDateTime timestamp;

	private ApiResponse (boolean success, T data, ErrorResponse error, LocalDateTime timestamp) {
		this.success = success;
		this.data = data;
		this.error = error;
		this.timestamp = timestamp;
	}

	/**
	 * 성공 응답 생성
	 */
	public static <T> ApiResponse<T> success (T data) {
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
	public static <T> ApiResponse<T> success () {
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
	public static <T> ApiResponse<T> failure (ErrorResponse error) {
		return new ApiResponse<>(
				false,
				null,
				error,
				LocalDateTime.now()
		);
	}
}