package innercircle.commerce.product.admin.application.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3 이미지 서비스 테스트")
class S3ImageServiceTest {

    @Mock
    private AmazonS3Client amazonS3Client;

    private S3ImageService s3ImageService;
    private final String bucketName = "test-bucket";
    private final String baseUrl = "https://test-bucket.s3.amazonaws.com";

    @BeforeEach
    void setUp() {
        s3ImageService = new S3ImageService(amazonS3Client, bucketName, baseUrl);
    }

    @Test
    @DisplayName("임시 이미지 업로드 성공")
    void 임시_이미지_업로드_성공() throws IOException {
        // given
        byte[] content = {1, 2, 3, 4};
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", content
        );

        // when
        S3ImageService.UploadedImageInfo result = s3ImageService.uploadToTemp(file);

        // then
        assertThat(result.tempId()).isNotNull();
        assertThat(result.originalName()).isEqualTo("test.jpg");
        assertThat(result.url()).startsWith(baseUrl + "/commerce/temp/images/");
        assertThat(result.url()).endsWith("/original.jpg");
        assertThat(result.size()).isEqualTo(4);
        assertThat(result.contentType()).isEqualTo("image/jpeg");

        // S3 업로드 호출 확인
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3Client).putObject(requestCaptor.capture());
        
        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getBucketName()).isEqualTo(bucketName);
        assertThat(capturedRequest.getKey()).contains("commerce/temp/images/");
        assertThat(capturedRequest.getKey()).endsWith("/original.jpg");
    }

    @Test
    @DisplayName("임시 이미지를 상품 경로로 이동 성공")
    void 임시_이미지를_상품_경로로_이동_성공() {
        // given
        String tempId = "temp-uuid-123";
        Long productId = 100L;
        Long imageId = 1L;
        String tempKey = "commerce/temp/images/" + tempId + "/original.jpg";
        
        when(amazonS3Client.doesObjectExist(bucketName, tempKey)).thenReturn(true);

        // when
        String result = s3ImageService.moveToProduct(tempId, productId, imageId);

        // then
        String expectedUrl = baseUrl + "/commerce/products/100/1.jpg";
        assertThat(result).isEqualTo(expectedUrl);

        // 복사와 삭제 호출 확인
        verify(amazonS3Client).copyObject(eq(bucketName), eq(tempKey), eq(bucketName), eq("commerce/products/100/1.jpg"));
        verify(amazonS3Client).deleteObject(eq(bucketName), eq(tempKey));
    }

    @Test
    @DisplayName("존재하지 않는 임시 파일 이동 시 예외 발생")
    void 존재하지_않는_임시_파일_이동_시_예외_발생() {
        // given
        String tempId = "non-existent-uuid";
        Long productId = 100L;
        Long imageId = 1L;
        
        // 모든 확장자에 대해 false 반환
        when(amazonS3Client.doesObjectExist(eq(bucketName), anyString())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> s3ImageService.moveToProduct(tempId, productId, imageId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("임시 파일을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("임시 이미지들 일괄 삭제 성공")
    void 임시_이미지들_일괄_삭제_성공() {
        // given
        List<String> tempIds = Arrays.asList("temp1", "temp2", "temp3");
        
        // temp1, temp2는 존재하고 temp3는 존재하지 않음
        when(amazonS3Client.doesObjectExist(bucketName, "commerce/temp/images/temp1/original.jpg")).thenReturn(true);
        when(amazonS3Client.doesObjectExist(bucketName, "commerce/temp/images/temp2/original.jpg")).thenReturn(true);
        when(amazonS3Client.doesObjectExist(eq(bucketName), contains("temp3"))).thenReturn(false);

        // when
        s3ImageService.deleteTempImages(tempIds);

        // then
        // temp3는 찾을 수 없어도 예외가 전파되지 않아야 함
        verify(amazonS3Client, times(2)).deleteObject(any()); // temp1, temp2만 삭제
    }

    @Test
    @DisplayName("상품 이미지들 일괄 삭제 성공")
    void 상품_이미지들_일괄_삭제_성공() {
        // given
        Long productId = 100L;
        List<Long> imageIds = Arrays.asList(1L, 2L);
        
        when(amazonS3Client.doesObjectExist(bucketName, "commerce/products/100/1.jpg")).thenReturn(true);
        when(amazonS3Client.doesObjectExist(bucketName, "commerce/products/100/2.jpg")).thenReturn(true);

        // when
        s3ImageService.deleteProductImages(productId, imageIds);

        // then
        verify(amazonS3Client, times(2)).deleteObject(any());
    }

    @Test
    @DisplayName("다양한 확장자의 파일 처리")
    void 다양한_확장자의_파일_처리() throws IOException {
        // given - PNG 파일
        MockMultipartFile pngFile = new MockMultipartFile(
                "image", "test.png", "image/png", new byte[]{1, 2, 3}
        );

        // when
        S3ImageService.UploadedImageInfo result = s3ImageService.uploadToTemp(pngFile);

        // then
        assertThat(result.url()).endsWith("/original.png");
        
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3Client).putObject(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getKey()).endsWith("/original.png");
    }

    @Test
    @DisplayName("확장자가 없는 파일명 처리")
    void 확장자가_없는_파일명_처리() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "image", "noextension", "image/jpeg", new byte[]{1, 2, 3}
        );

        // when
        S3ImageService.UploadedImageInfo result = s3ImageService.uploadToTemp(file);

        // then
        assertThat(result.url()).endsWith("/original.jpg"); // 기본값 jpg 사용
    }
}