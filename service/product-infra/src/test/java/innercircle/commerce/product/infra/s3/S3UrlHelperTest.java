package innercircle.commerce.product.infra.s3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3UrlHelperTest {

    private final S3UrlHelper s3UrlHelper = new S3UrlHelper();

    @Nested
    @DisplayName("URL에서 S3 키 추출")
    class ExtractKeyFromUrl {

        @Test
        @DisplayName("정상적인 S3 URL에서 키를 추출할 수 있다.")
        void URL에서_키_추출_성공() {
            // given
            String s3Url = "https://bucket.s3.region.amazonaws.com/commerce/temp/123/image.jpg";

            // when
            String result = s3UrlHelper.extractKeyFromUrl(s3Url);

            // then
            assertThat(result).isEqualTo("commerce/temp/123/image.jpg");
        }

        @Test
        @DisplayName("빈 URL인 경우 예외가 발생한다.")
        void 빈_URL_예외() {
            // given
            String emptyUrl = "";

            // when & then
            assertThatThrownBy(() -> s3UrlHelper.extractKeyFromUrl(emptyUrl))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("URL이 비어있습니다.");
        }

        @Test
        @DisplayName("null URL인 경우 예외가 발생한다.")
        void null_URL_예외() {
            // given
            String nullUrl = null;

            // when & then
            assertThatThrownBy(() -> s3UrlHelper.extractKeyFromUrl(nullUrl))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("URL이 비어있습니다.");
        }

        @Test
        @DisplayName("유효하지 않은 S3 URL인 경우 예외가 발생한다.")
        void 유효하지_않은_URL_예외() {
            // given
            String invalidUrl = "https://example.com/image.jpg";

            // when & then
            assertThatThrownBy(() -> s3UrlHelper.extractKeyFromUrl(invalidUrl))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 S3 URL입니다:");
        }
    }

    @Nested
    @DisplayName("복잡한 방식으로 URL에서 S3 키 추출")
    class ExtractKeyFromUrlComplex {

        @Test
        @DisplayName("정상적인 S3 URL에서 키를 추출할 수 있다.")
        void 복잡한_방식_URL에서_키_추출_성공() {
            // given
            String s3Url = "https://bucket.s3.region.amazonaws.com/commerce/temp/123/image.jpg";

            // when
            String result = s3UrlHelper.extractKeyFromUrlComplex(s3Url);

            // then
            assertThat(result).isEqualTo("commerce/temp/123/image.jpg");
        }

        @Test
        @DisplayName("빈 URL인 경우 예외가 발생한다.")
        void 복잡한_방식_빈_URL_예외() {
            // given
            String emptyUrl = "";

            // when & then
            assertThatThrownBy(() -> s3UrlHelper.extractKeyFromUrlComplex(emptyUrl))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("URL이 비어있습니다.");
        }

        @Test
        @DisplayName("유효하지 않은 S3 URL인 경우 예외가 발생한다.")
        void 복잡한_방식_유효하지_않은_URL_예외() {
            // given
            String invalidUrl = "https://example.com/image.jpg";

            // when & then
            assertThatThrownBy(() -> s3UrlHelper.extractKeyFromUrlComplex(invalidUrl))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유효하지 않은 S3 URL입니다: " + invalidUrl);
        }
    }

    @Nested
    @DisplayName("상품 이미지 키 생성")
    class BuildProductImageKey {

        @Test
        @DisplayName("정상적인 파라미터로 상품 이미지 키를 생성할 수 있다.")
        void 상품_이미지_키_생성_성공() {
            // given
            Long productId = 123L;
            Long imageId = 456L;
            String extension = "jpg";

            // when
            String result = s3UrlHelper.buildProductImageKey(productId, imageId, extension);

            // then
            assertThat(result).isEqualTo("commerce/products/123/456.jpg");
        }

        @Test
        @DisplayName("확장자가 대문자인 경우 소문자로 변환된다.")
        void 확장자_소문자_변환() {
            // given
            Long productId = 123L;
            Long imageId = 456L;
            String extension = "PNG";

            // when
            String result = s3UrlHelper.buildProductImageKey(productId, imageId, extension);

            // then
            assertThat(result).isEqualTo("commerce/products/123/456.png");
        }

        @Test
        @DisplayName("확장자가 비어있는 경우 기본값 jpg를 사용한다.")
        void 확장자_기본값_사용() {
            // given
            Long productId = 123L;
            Long imageId = 456L;
            String extension = "";

            // when
            String result = s3UrlHelper.buildProductImageKey(productId, imageId, extension);

            // then
            assertThat(result).isEqualTo("commerce/products/123/456.jpg");
        }

        @Test
        @DisplayName("상품 ID가 null인 경우 예외가 발생한다.")
        void 상품_ID_null_예외() {
            // given
            Long productId = null;
            Long imageId = 456L;
            String extension = "jpg";

            // when & then
            assertThatThrownBy(() -> s3UrlHelper.buildProductImageKey(productId, imageId, extension))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("상품 ID와 이미지 ID는 필수입니다.");
        }

        @Test
        @DisplayName("이미지 ID가 null인 경우 예외가 발생한다.")
        void 이미지_ID_null_예외() {
            // given
            Long productId = 123L;
            Long imageId = null;
            String extension = "jpg";

            // when & then
            assertThatThrownBy(() -> s3UrlHelper.buildProductImageKey(productId, imageId, extension))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("상품 ID와 이미지 ID는 필수입니다.");
        }
    }

    @Nested
    @DisplayName("파일명에서 확장자 추출")
    class ExtractExtensionFromFilename {

        @Test
        @DisplayName("정상적인 파일명에서 확장자를 추출할 수 있다.")
        void 파일명에서_확장자_추출_성공() {
            // given
            String filename = "image.jpg";

            // when
            String result = s3UrlHelper.extractExtensionFromFilename(filename);

            // then
            assertThat(result).isEqualTo("jpg");
        }

        @Test
        @DisplayName("확장자가 대문자인 경우 소문자로 변환된다.")
        void 확장자_대문자_소문자_변환() {
            // given
            String filename = "image.PNG";

            // when
            String result = s3UrlHelper.extractExtensionFromFilename(filename);

            // then
            assertThat(result).isEqualTo("png");
        }

        @Test
        @DisplayName("쿼리 파라미터가 있는 경우 제거된다.")
        void 쿼리_파라미터_제거() {
            // given
            String filename = "image.jpg?v=123";

            // when
            String result = s3UrlHelper.extractExtensionFromFilename(filename);

            // then
            assertThat(result).isEqualTo("jpg");
        }

        @Test
        @DisplayName("확장자가 없는 파일명인 경우 기본값을 반환한다.")
        void 확장자_없는_파일명_기본값() {
            // given
            String filename = "image";

            // when
            String result = s3UrlHelper.extractExtensionFromFilename(filename);

            // then
            assertThat(result).isEqualTo("jpg");
        }

        @Test
        @DisplayName("빈 파일명인 경우 기본값을 반환한다.")
        void 빈_파일명_기본값() {
            // given
            String filename = "";

            // when
            String result = s3UrlHelper.extractExtensionFromFilename(filename);

            // then
            assertThat(result).isEqualTo("jpg");
        }
    }

    @Nested
    @DisplayName("URL 또는 파일명에서 확장자 추출")
    class ExtractExtensionFromUrlOrName {

        @Test
        @DisplayName("URL에서 확장자를 우선적으로 추출한다.")
        void URL_우선_확장자_추출() {
            // given
            String url = "https://example.com/image.png";
            String originalName = "original.jpg";

            // when
            String result = s3UrlHelper.extractExtensionFromUrlOrName(url, originalName);

            // then
            assertThat(result).isEqualTo("png");
        }

        @Test
        @DisplayName("URL에서 확장자를 찾을 수 없으면 원본 파일명에서 추출한다.")
        void 원본_파일명에서_확장자_추출() {
            // given
            String url = "https://example.com/image";  // 확장자 없는 URL
            String originalName = "original.gif";

            // when
            String result = s3UrlHelper.extractExtensionFromUrlOrName(url, originalName);

            // then
            assertThat(result).isEqualTo("gif");
        }

        @Test
        @DisplayName("URL과 원본 파일명 모두에서 확장자를 찾을 수 없으면 기본값을 반환한다.")
        void 모두_확장자_없으면_기본값() {
            // given
            String url = "https://example.com/image";
            String originalName = "original";

            // when
            String result = s3UrlHelper.extractExtensionFromUrlOrName(url, originalName);

            // then
            assertThat(result).isEqualTo("jpg");
        }

        @Test
        @DisplayName("URL이 null이면 원본 파일명에서만 확장자를 추출한다.")
        void URL_null이면_원본_파일명_사용() {
            // given
            String url = null;
            String originalName = "original.webp";

            // when
            String result = s3UrlHelper.extractExtensionFromUrlOrName(url, originalName);

            // then
            assertThat(result).isEqualTo("webp");
        }
    }
}