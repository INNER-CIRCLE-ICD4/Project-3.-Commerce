package innercircle.commerce.product.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TempImageValidator 테스트")
class TempImageValidatorTest {

	@Nested
	@DisplayName("원본 파일명 검증")
	class ValidateOriginalName {

		@Test
		@DisplayName("정상적인 파일명은 통과한다.")
		void 정상_파일명_통과() {
			// when & then
			assertThatNoException()
					.isThrownBy(() -> TempImageValidator.validateOriginalName("test.jpg"));
		}

		@Test
		@DisplayName("한글 파일명도 통과한다.")
		void 한글_파일명_통과() {
			// when & then
			assertThatNoException()
					.isThrownBy(() -> TempImageValidator.validateOriginalName("테스트파일.jpg"));
		}

		@Test
		@DisplayName("255자 파일명은 통과한다.")
		void 최대길이_파일명_통과() {
			// given
			String maxLengthName = "a".repeat(255);

			// when & then
			assertThatNoException()
					.isThrownBy(() -> TempImageValidator.validateOriginalName(maxLengthName));
		}

		@Test
		@DisplayName("null 파일명은 허용된다.")
		void null_파일명_허용() {
			// when & then
			assertThatNoException()
					.isThrownBy(() -> TempImageValidator.validateOriginalName(null));
		}

		@Test
		@DisplayName("빈 문자열 파일명은 예외가 발생한다.")
		void 빈문자열_파일명_예외() {
			// when & then
			assertThatThrownBy(() -> TempImageValidator.validateOriginalName(""))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("원본 파일명은 공백일 수 없습니다");
		}

		@Test
		@DisplayName("공백만 있는 파일명은 예외가 발생한다.")
		void 공백_파일명_예외() {
			// when & then
			assertThatThrownBy(() -> TempImageValidator.validateOriginalName("   "))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("원본 파일명은 공백일 수 없습니다");
		}

		@Test
		@DisplayName("255자를 초과하는 파일명은 예외가 발생한다.")
		void 길이초과_파일명_예외() {
			// given
			String tooLongName = "a".repeat(256);

			// when & then
			assertThatThrownBy(() -> TempImageValidator.validateOriginalName(tooLongName))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("원본 파일명은 255자를 초과할 수 없습니다");
		}
	}

	@Nested
	@DisplayName("URL 검증")
	class ValidateUrl {

		@Test
		@DisplayName("HTTP URL은 통과한다.")
		void HTTP_URL_통과() {
			// when & then
			assertThatNoException()
					.isThrownBy(() -> TempImageValidator.validateUrl("http://example.com/test.jpg"));
		}

		@Test
		@DisplayName("HTTPS URL은 통과한다.")
		void HTTPS_URL_통과() {
			// when & then
			assertThatNoException()
					.isThrownBy(() -> TempImageValidator.validateUrl("https://example.com/test.jpg"));
		}

		@Test
		@DisplayName("S3 URL 형식도 통과한다.")
		void S3_URL_통과() {
			// when & then
			assertThatNoException()
					.isThrownBy(() -> TempImageValidator.validateUrl("https://bucket.s3.amazonaws.com/path/test.jpg"));
		}

		@Test
		@DisplayName("2000자 URL은 통과한다.")
		void 최대길이_URL_통과() {
			// given
			String maxLengthUrl = "https://example.com/" + "a".repeat(1977); // 전체 2000자

			// when & then
			assertThatNoException()
					.isThrownBy(() -> TempImageValidator.validateUrl(maxLengthUrl));
		}

		@Test
		@DisplayName("null URL은 허용된다.")
		void null_URL_허용() {
			// when & then
			assertThatNoException()
					.isThrownBy(() -> TempImageValidator.validateUrl(null));
		}

		@Test
		@DisplayName("빈 문자열 URL은 예외가 발생한다.")
		void 빈문자열_URL_예외() {
			// when & then
			assertThatThrownBy(() -> TempImageValidator.validateUrl(""))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("이미지 URL은 필수입니다");
		}

		@Test
		@DisplayName("공백만 있는 URL은 예외가 발생한다.")
		void 공백_URL_예외() {
			// when & then
			assertThatThrownBy(() -> TempImageValidator.validateUrl("   "))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("이미지 URL은 필수입니다");
		}

		@Test
		@DisplayName("HTTP/HTTPS가 아닌 URL은 예외가 발생한다.")
		void 잘못된_프로토콜_URL_예외() {
			// when & then
			assertThatThrownBy(() -> TempImageValidator.validateUrl("ftp://example.com/test.jpg"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("올바른 URL 형식이 아닙니다");
		}

		@Test
		@DisplayName("프로토콜이 없는 URL은 예외가 발생한다.")
		void 프로토콜없는_URL_예외() {
			// when & then
			assertThatThrownBy(() -> TempImageValidator.validateUrl("example.com/test.jpg"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("올바른 URL 형식이 아닙니다");
		}

		@Test
		@DisplayName("2000자를 초과하는 URL은 예외가 발생한다.")
		void 길이초과_URL_예외() {
			// given
			String tooLongUrl = "https://example.com/" + "a".repeat(2000); // 전체 2000자 초과

			// when & then
			assertThatThrownBy(() -> TempImageValidator.validateUrl(tooLongUrl))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("URL은 2000자를 초과할 수 없습니다");
		}
	}
}