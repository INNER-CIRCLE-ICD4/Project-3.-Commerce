package innercircle.commerce.product.admin.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("이미지 업로드 명령 테스트")
class ImageUploadCommandTest {

    @Test
    @DisplayName("정상적인 이미지 업로드 명령 생성 성공")
    void 정상적인_이미지_업로드_명령_생성_성공() {
        // given
        List<MultipartFile> files = Arrays.asList(
                new MockMultipartFile("main", "main.jpg", "image/jpeg", new byte[]{1, 2, 3}),
                new MockMultipartFile("sub", "sub.jpg", "image/jpeg", new byte[]{4, 5, 6})
        );
        
        List<ImageUploadCommand.ImageMetadata> metadata = Arrays.asList(
                new ImageUploadCommand.ImageMetadata(true, 1),   // 대표 이미지
                new ImageUploadCommand.ImageMetadata(false, 2)   // 일반 이미지
        );

        // when & then
        assertThatNoException().isThrownBy(() -> 
                new ImageUploadCommand(files, metadata)
        );
    }

    @Test
    @DisplayName("파일이 비어있으면 예외 발생")
    void 파일이_비어있으면_예외_발생() {
        // given
        List<MultipartFile> emptyFiles = Collections.emptyList();
        List<ImageUploadCommand.ImageMetadata> metadata = Arrays.asList(
                new ImageUploadCommand.ImageMetadata(true, 1)
        );

        // when & then
        assertThatThrownBy(() -> new ImageUploadCommand(emptyFiles, metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("업로드할 이미지 파일이 필요합니다");
    }

    @Test
    @DisplayName("파일과 메타데이터 수가 일치하지 않으면 예외 발생")
    void 파일과_메타데이터_수가_일치하지_않으면_예외_발생() {
        // given
        List<MultipartFile> files = Arrays.asList(
                new MockMultipartFile("main", "main.jpg", "image/jpeg", new byte[]{1, 2, 3})
        );
        
        List<ImageUploadCommand.ImageMetadata> metadata = Arrays.asList(
                new ImageUploadCommand.ImageMetadata(true, 1),
                new ImageUploadCommand.ImageMetadata(false, 2)  // 파일보다 메타데이터가 많음
        );

        // when & then
        assertThatThrownBy(() -> new ImageUploadCommand(files, metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일 수와 메타데이터 수가 일치해야 합니다");
    }

    @Test
    @DisplayName("대표 이미지가 없으면 예외 발생")
    void 대표_이미지가_없으면_예외_발생() {
        // given
        List<MultipartFile> files = Arrays.asList(
                new MockMultipartFile("sub1", "sub1.jpg", "image/jpeg", new byte[]{1, 2, 3}),
                new MockMultipartFile("sub2", "sub2.jpg", "image/jpeg", new byte[]{4, 5, 6})
        );
        
        List<ImageUploadCommand.ImageMetadata> metadata = Arrays.asList(
                new ImageUploadCommand.ImageMetadata(false, 1),  // 모두 일반 이미지
                new ImageUploadCommand.ImageMetadata(false, 2)
        );

        // when & then
        assertThatThrownBy(() -> new ImageUploadCommand(files, metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대표 이미지가 반드시 1개 필요합니다");
    }

    @Test
    @DisplayName("대표 이미지가 2개 이상이면 예외 발생")
    void 대표_이미지가_2개_이상이면_예외_발생() {
        // given
        List<MultipartFile> files = Arrays.asList(
                new MockMultipartFile("main1", "main1.jpg", "image/jpeg", new byte[]{1, 2, 3}),
                new MockMultipartFile("main2", "main2.jpg", "image/jpeg", new byte[]{4, 5, 6})
        );
        
        List<ImageUploadCommand.ImageMetadata> metadata = Arrays.asList(
                new ImageUploadCommand.ImageMetadata(true, 1),   // 대표 이미지
                new ImageUploadCommand.ImageMetadata(true, 2)    // 또 다른 대표 이미지
        );

        // when & then
        assertThatThrownBy(() -> new ImageUploadCommand(files, metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대표 이미지가 반드시 1개 필요합니다");
    }

    @Test
    @DisplayName("정렬 순서가 1보다 작으면 예외 발생")
    void 정렬_순서가_1보다_작으면_예외_발생() {
        // when & then
        assertThatThrownBy(() -> new ImageUploadCommand.ImageMetadata(true, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정렬 순서는 1 이상이어야 합니다");
    }

    @Test
    @DisplayName("이미지 메타데이터 정상 생성")
    void 이미지_메타데이터_정상_생성() {
        // when
        ImageUploadCommand.ImageMetadata metadata = new ImageUploadCommand.ImageMetadata(true, 1);

        // then
        assertThat(metadata.isMain()).isTrue();
        assertThat(metadata.sortOrder()).isEqualTo(1);
    }
}