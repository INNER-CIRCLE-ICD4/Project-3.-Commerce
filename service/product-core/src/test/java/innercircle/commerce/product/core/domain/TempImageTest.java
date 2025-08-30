package innercircle.commerce.product.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TempImage 도메인 테스트")
class TempImageTest {

	@Nested
	@DisplayName("임시 이미지 생성")
	class Create {

		@Test
		@DisplayName("정상적인 입력으로 임시 이미지를 생성할 수 있다.")
		void 임시_이미지_생성_성공() {
			// given
			String originalName = "test.jpg";
			String url = "https://s3.amazonaws.com/bucket/temp/123/test.jpg";

			// when
			TempImage tempImage = TempImage.create(originalName, url);

			// then
			assertThat(tempImage.getId()).isNotNull();
			assertThat(tempImage.getOriginalName()).isEqualTo(originalName);
			assertThat(tempImage.getUrl()).isEqualTo(url);
		}

		@Test
		@DisplayName("ID는 자동으로 생성된다.")
		void ID_자동_생성() {
			// when
			TempImage tempImage1 = TempImage.create("test1.jpg", "https://example.com/1");
			TempImage tempImage2 = TempImage.create("test2.jpg", "https://example.com/2");

			// then
			assertThat(tempImage1.getId()).isNotNull();
			assertThat(tempImage2.getId()).isNotNull();
			assertThat(tempImage1.getId()).isNotEqualTo(tempImage2.getId());
		}

		@Test
		@DisplayName("원본 파일명이 null이어도 생성할 수 있다.")
		void 원본_파일명_null_허용() {
			// when & then
			assertThatNoException()
					.isThrownBy(() -> TempImage.create(null, "https://example.com/test"));
		}

		@Test
		@DisplayName("원본 파일명이 빈 문자열이면 예외가 발생한다.")
		void 원본_파일명_빈문자열_예외() {
			// when & then
			assertThatThrownBy(() -> TempImage.create("", "https://example.com/test"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("원본 파일명은 공백일 수 없습니다");
		}

		@Test
		@DisplayName("원본 파일명이 255자를 초과하면 예외가 발생한다.")
		void 원본_파일명_길이_초과_예외() {
			// given
			String longName = "a".repeat(256);

			// when & then
			assertThatThrownBy(() -> TempImage.create(longName, "https://example.com/test"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("원본 파일명은 255자를 초과할 수 없습니다");
		}

		@Test
		@DisplayName("URL이 null이어도 생성할 수 있다.")
		void URL_null_허용() {
			// when & then
			assertThatNoException()
					.isThrownBy(() -> TempImage.create("test.jpg", null));
		}

		@Test
		@DisplayName("URL이 빈 문자열이면 예외가 발생한다.")
		void URL_빈문자열_예외() {
			// when & then
			assertThatThrownBy(() -> TempImage.create("test.jpg", ""))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("이미지 URL은 필수입니다");
		}

		@Test
		@DisplayName("올바르지 않은 URL 형식이면 예외가 발생한다.")
		void 잘못된_URL_형식_예외() {
			// when & then
			assertThatThrownBy(() -> TempImage.create("test.jpg", "invalid-url"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("올바른 URL 형식이 아닙니다");
		}

		@Test
		@DisplayName("URL이 2000자를 초과하면 예외가 발생한다.")
		void URL_길이_초과_예외() {
			// given
			String longUrl = "https://example.com/" + "a".repeat(2000);

			// when & then
			assertThatThrownBy(() -> TempImage.create("test.jpg", longUrl))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("URL은 2000자를 초과할 수 없습니다");
		}
	}

	@Nested
	@DisplayName("임시 이미지 복원")
	class Restore {

		@Test
		@DisplayName("기존 정보로 임시 이미지를 복원할 수 있다.")
		void 임시_이미지_복원_성공() {
			// given
			Long existingId = 123L;
			String originalName = "existing.jpg";
			String url = "https://s3.amazonaws.com/bucket/existing.jpg";

			// when
			TempImage tempImage = TempImage.restore(existingId, originalName, url);

			// then
			assertThat(tempImage.getId()).isEqualTo(existingId);
			assertThat(tempImage.getOriginalName()).isEqualTo(originalName);
			assertThat(tempImage.getUrl()).isEqualTo(url);
		}

		@Test
		@DisplayName("복원 시에도 검증이 수행된다.")
		void 복원시_검증_수행() {
			// when & then
			assertThatThrownBy(() -> TempImage.restore(123L, "", "https://example.com/test"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("원본 파일명은 공백일 수 없습니다");
		}
	}
}