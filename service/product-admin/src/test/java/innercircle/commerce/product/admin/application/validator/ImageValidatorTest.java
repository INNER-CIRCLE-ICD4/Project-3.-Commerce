package innercircle.commerce.product.admin.application.validator;

import innercircle.commerce.product.admin.application.exception.InvalidImageFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("이미지 파일 검증 테스트")
class ImageValidatorTest {

    private ImageValidator imageValidator;

    @BeforeEach
    void setUp() {
        imageValidator = new ImageValidator();
        ReflectionTestUtils.setField(imageValidator, "maxFileSize", 5242880L); // 5MB
    }

    @Test
    @DisplayName("정상적인 JPEG 파일 검증 성공")
    void 정상적인_JPEG_파일_검증_성공() {
        // given
        byte[] jpegBytes = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}; // JPEG 시그니처
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", jpegBytes
        );

        // when & then
        assertThatNoException().isThrownBy(() -> imageValidator.validate(file));
    }

    @Test
    @DisplayName("정상적인 PNG 파일 검증 성공")
    void 정상적인_PNG_파일_검증_성공() {
        // given
        byte[] pngBytes = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}; // PNG 시그니처
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.png", "image/png", pngBytes
        );

        // when & then
        assertThatNoException().isThrownBy(() -> imageValidator.validate(file));
    }

    @Test
    @DisplayName("빈 파일은 검증 실패")
    void 빈_파일은_검증_실패() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[0]
        );

        // when & then
        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(InvalidImageFileException.class)
                .hasFieldOrPropertyWithValue("reason", "EMPTY_FILE");
    }

    @Test
    @DisplayName("지원하지 않는 확장자는 검증 실패")
    void 지원하지_않는_확장자는_검증_실패() {
        // given
        byte[] content = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.bmp", "image/bmp", content
        );

        // when & then
        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(InvalidImageFileException.class)
                .hasFieldOrPropertyWithValue("reason", "INVALID_EXTENSION");
    }

    @Test
    @DisplayName("파일 크기 초과 시 검증 실패")
    void 파일_크기_초과_시_검증_실패() {
        // given
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        largeContent[0] = (byte) 0xFF;
        largeContent[1] = (byte) 0xD8;
        largeContent[2] = (byte) 0xFF;
        largeContent[3] = (byte) 0xE0;
        
        MockMultipartFile file = new MockMultipartFile(
                "image", "large.jpg", "image/jpeg", largeContent
        );

        // when & then
        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(InvalidImageFileException.class)
                .hasFieldOrPropertyWithValue("reason", "FILE_TOO_LARGE");
    }

    @Test
    @DisplayName("유효하지 않은 Content-Type은 검증 실패")
    void 유효하지_않은_ContentType은_검증_실패() {
        // given
        byte[] content = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "text/plain", content
        );

        // when & then
        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(InvalidImageFileException.class)
                .hasFieldOrPropertyWithValue("reason", "INVALID_CONTENT_TYPE");
    }

    @Test
    @DisplayName("파일 시그니처가 일치하지 않으면 검증 실패")
    void 파일_시그니처가_일치하지_않으면_검증_실패() {
        // given
        byte[] invalidContent = {0x00, 0x01, 0x02, 0x03}; // 유효하지 않은 시그니처
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", invalidContent
        );

        // when & then
        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(InvalidImageFileException.class)
                .hasFieldOrPropertyWithValue("reason", "INVALID_FILE_SIGNATURE");
    }

    @Test
    @DisplayName("파일명이 없으면 검증 실패")
    void 파일명이_없으면_검증_실패() {
        // given
        byte[] content = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        MockMultipartFile file = new MockMultipartFile(
                "image", null, "image/jpeg", content
        );

        // when & then
        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(InvalidImageFileException.class)
                .hasFieldOrPropertyWithValue("reason", "MISSING_FILENAME");
    }

    @Test
    @DisplayName("WebP 파일 검증 성공")
    void WebP_파일_검증_성공() {
        // given
        byte[] webpBytes = {
                0x52, 0x49, 0x46, 0x46, // RIFF
                0x00, 0x00, 0x00, 0x00, // 파일 크기 (더미)
                0x57, 0x45, 0x42, 0x50  // WEBP
        };
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.webp", "image/webp", webpBytes
        );

        // when & then
        assertThatNoException().isThrownBy(() -> imageValidator.validate(file));
    }
}