package innercircle.commerce.product.infra.s3;

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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3 이미지 저장소 테스트")
class S3ImageStoreTest {

    @Mock
    private AmazonS3Client amazonS3Client;

    private S3ImageStore s3ImageStore;
    private final String bucketName = "test-bucket";
    private final String baseUrl = "https://test-bucket.s3.amazonaws.com";

    @BeforeEach
    void setUp() {
        s3ImageStore = new S3ImageStore(amazonS3Client, bucketName);
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void 이미지_업로드_성공() throws IOException {
        // given
        byte[] content = {1, 2, 3, 4};
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", content
        );
        String region = "us-east-1";
        String s3Key = "commerce/temp/images/temp-id/original.jpg";
        given(amazonS3Client.getRegionName()).willReturn(region);

        // when
        String result = s3ImageStore.upload(file, s3Key);

        // then
        String expectedUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
        assertThat(result).isEqualTo(expectedUrl);

        // S3 업로드 호출 확인
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3Client).putObject(requestCaptor.capture());
        
        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getBucketName()).isEqualTo(bucketName);
        assertThat(capturedRequest.getKey()).isEqualTo(s3Key);
    }

    @Test
    @DisplayName("이미지 이동 성공")
    void 이미지_이동_성공() {
        // given
        String sourceKey = "commerce/temp/images/temp-uuid-123/original.jpg";
        String targetKey = "commerce/products/100/1.jpg";
        String region = "us-east-1";
        
        when(amazonS3Client.doesObjectExist(bucketName, sourceKey)).thenReturn(true);
        given(amazonS3Client.getRegionName()).willReturn(region);

        // when
        var result = s3ImageStore.move(sourceKey, targetKey);

        // then
        assertThat(result).isPresent();
        String expectedUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, targetKey);
        assertThat(result.get()).isEqualTo(expectedUrl);

        // 복사와 삭제 호출 확인
        verify(amazonS3Client).copyObject(eq(bucketName), eq(sourceKey), eq(bucketName), eq(targetKey));
        verify(amazonS3Client).deleteObject(eq(bucketName), eq(sourceKey));
    }

    @Test
    @DisplayName("존재하지 않는 파일 이동 시 빈 Optional 반환")
    void 존재하지_않는_파일_이동_시_빈_Optional_반환() {
        // given
        String sourceKey = "commerce/temp/images/non-existent/original.jpg";
        String targetKey = "commerce/products/100/1.jpg";
        
        when(amazonS3Client.doesObjectExist(bucketName, sourceKey)).thenReturn(false);

        // when
        var result = s3ImageStore.move(sourceKey, targetKey);

        // then
        assertThat(result).isEmpty();
        verify(amazonS3Client, never()).copyObject(anyString(), anyString(), anyString(), anyString());
        verify(amazonS3Client, never()).deleteObject(anyString(), anyString());
    }

    @Test
    @DisplayName("이미지들 일괄 삭제 성공")
    void 이미지들_일괄_삭제_성공() {
        // given
        List<String> s3Keys = Arrays.asList(
                "commerce/temp/images/temp1/original.jpg",
                "commerce/temp/images/temp2/original.jpg",
                "commerce/temp/images/temp3/original.jpg"
        );
        
        // temp1, temp2는 존재하고 temp3는 존재하지 않음
        when(amazonS3Client.doesObjectExist(bucketName, s3Keys.get(0))).thenReturn(true);
        when(amazonS3Client.doesObjectExist(bucketName, s3Keys.get(1))).thenReturn(true);
        when(amazonS3Client.doesObjectExist(bucketName, s3Keys.get(2))).thenReturn(false);

        // when
        s3ImageStore.delete(s3Keys);

        // then
        // temp3는 존재하지 않으므로 삭제되지 않음
        verify(amazonS3Client, times(2)).deleteObject(any()); // temp1, temp2만 삭제
    }

    @Test
    @DisplayName("단일 이미지 삭제 성공")
    void 단일_이미지_삭제_성공() {
        // given
        String s3Key = "commerce/products/100/1.jpg";
        
        when(amazonS3Client.doesObjectExist(bucketName, s3Key)).thenReturn(true);

        // when
        s3ImageStore.delete(s3Key);

        // then
        verify(amazonS3Client, times(1)).deleteObject(any());
    }

    @Test
    @DisplayName("객체 존재 여부 확인")
    void 객체_존재_여부_확인() {
        // given
        String s3Key = "commerce/products/100/1.jpg";
        
        when(amazonS3Client.doesObjectExist(bucketName, s3Key)).thenReturn(true);

        // when
        boolean exists = s3ImageStore.exists(s3Key);

        // then
        assertThat(exists).isTrue();
        verify(amazonS3Client).doesObjectExist(bucketName, s3Key);
    }

    @Test
    @DisplayName("존재하지 않는 객체 존재 여부 확인")
    void 존재하지_않는_객체_존재_여부_확인() {
        // given
        String s3Key = "commerce/products/999/non-existent.jpg";
        
        when(amazonS3Client.doesObjectExist(bucketName, s3Key)).thenReturn(false);

        // when
        boolean exists = s3ImageStore.exists(s3Key);

        // then
        assertThat(exists).isFalse();
        verify(amazonS3Client).doesObjectExist(bucketName, s3Key);
    }
}