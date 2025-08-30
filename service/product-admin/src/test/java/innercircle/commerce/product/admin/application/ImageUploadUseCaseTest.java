package innercircle.commerce.product.admin.application;

import innercircle.commerce.product.admin.application.dto.ImageUploadInfo;
import innercircle.commerce.product.admin.application.validator.ImageValidator;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("이미지 업로드 UseCase 테스트")
class ImageUploadUseCaseTest {

	@Mock
	private S3ImageStore s3ImageStore;

	@Mock
	private ImageValidator imageValidator;

	@InjectMocks
	private ImageUploadUseCase imageUploadUseCase;

	@Nested
	@DisplayName("임시 업로드")
	class UploadToTemp {

		@Test
		@DisplayName("단일 이미지 파일을 정상적으로 업로드할 수 있다.")
		void 단일_이미지_업로드_성공 () throws IOException {
			// given
			MultipartFile file = mock(MultipartFile.class);
			given(file.getOriginalFilename()).willReturn("test.jpg");

			String expectedUrl = "https://s3.amazonaws.com/bucket/commerce/temp/images/123/original.jpg";
			given(s3ImageStore.upload(any(MultipartFile.class), any(String.class)))
					.willReturn(expectedUrl);

			List<MultipartFile> files = Collections.singletonList(file);

			// when
			List<ImageUploadInfo> result = imageUploadUseCase.uploadToTemp(files);

			// then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).originalName()).isEqualTo("test.jpg");
			assertThat(result.get(0).url()).isEqualTo(expectedUrl);
			assertThat(result.get(0).id()).isNotNull();

			then(imageValidator).should().validate(eq(file));
			then(s3ImageStore).should().upload(eq(file), any(String.class));
		}

		@Test
		@DisplayName("여러 이미지 파일을 정상적으로 업로드할 수 있다.")
		void 다중_이미지_업로드_성공 () throws IOException {
			// given
			MultipartFile file1 = mock(MultipartFile.class);
			MultipartFile file2 = mock(MultipartFile.class);
			given(file1.getOriginalFilename()).willReturn("test1.jpg");
			given(file2.getOriginalFilename()).willReturn("test2.png");

			String expectedUrl1 = "https://s3.amazonaws.com/bucket/commerce/temp/images/123/original.jpg";
			String expectedUrl2 = "https://s3.amazonaws.com/bucket/commerce/temp/images/124/original.png";
			given(s3ImageStore.upload(eq(file1), any(String.class))).willReturn(expectedUrl1);
			given(s3ImageStore.upload(eq(file2), any(String.class))).willReturn(expectedUrl2);

			List<MultipartFile> files = Arrays.asList(file1, file2);

			// when
			List<ImageUploadInfo> result = imageUploadUseCase.uploadToTemp(files);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get(0).originalName()).isEqualTo("test1.jpg");
			assertThat(result.get(0).url()).isEqualTo(expectedUrl1);
			assertThat(result.get(1).originalName()).isEqualTo("test2.png");
			assertThat(result.get(1).url()).isEqualTo(expectedUrl2);

			then(imageValidator).should().validate(eq(file1));
			then(imageValidator).should().validate(eq(file2));
			then(s3ImageStore).should().upload(eq(file1), any(String.class));
			then(s3ImageStore).should().upload(eq(file2), any(String.class));
		}

		@Test
		@DisplayName("S3 업로드 실패 시 예외가 발생한다.")
		void S3_업로드_실패 () throws IOException {
			// given
			MultipartFile file = mock(MultipartFile.class);
			given(file.getOriginalFilename()).willReturn("test.jpg");

			given(s3ImageStore.upload(any(MultipartFile.class), any(String.class)))
					.willThrow(new RuntimeException("S3 업로드 실패"));

			List<MultipartFile> files = Collections.singletonList(file);

			// when & then
			assertThatThrownBy(() -> imageUploadUseCase.uploadToTemp(files))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("S3 업로드 실패");

			then(imageValidator).should().validate(eq(file));
		}

		@Test
		@DisplayName("일부 파일 업로드 후 실패 시 업로드된 파일들을 삭제한다.")
		void 업로드_실패시_롤백 () throws IOException {
			// given
			MultipartFile file1 = mock(MultipartFile.class);
			MultipartFile file2 = mock(MultipartFile.class);
			given(file1.getOriginalFilename()).willReturn("test1.jpg");
			given(file2.getOriginalFilename()).willReturn("test2.jpg");

			String uploadedUrl1 = "https://s3.amazonaws.com/bucket/commerce/temp/images/123/original.jpg";
			given(s3ImageStore.upload(eq(file1), any(String.class))).willReturn(uploadedUrl1);
			given(s3ImageStore.upload(eq(file2), any(String.class)))
					.willThrow(new RuntimeException("두 번째 파일 업로드 실패"));

			List<MultipartFile> files = Arrays.asList(file1, file2);

			// when & then
			assertThatThrownBy(() -> imageUploadUseCase.uploadToTemp(files))
					.isInstanceOf(RuntimeException.class);

			then(s3ImageStore).should().delete(eq(List.of(uploadedUrl1)));
		}

		@Test
		@DisplayName("확장자가 없는 파일명의 경우 예외가 발생한다.")
		void 확장자_없는_파일명_예외 () throws IOException {
			// given
			MultipartFile file = mock(MultipartFile.class);
			given(file.getOriginalFilename()).willReturn("testfile");

			List<MultipartFile> files = Collections.singletonList(file);

			// when & then
			assertThatThrownBy(() -> imageUploadUseCase.uploadToTemp(files))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("파일 확장자가 없습니다");

			then(imageValidator).should().validate(eq(file));
		}

		@Test
		@DisplayName("null 파일명의 경우 예외가 발생한다.")
		void null_파일명_예외 () throws IOException {
			// given
			MultipartFile file = mock(MultipartFile.class);
			given(file.getOriginalFilename()).willReturn(null);

			List<MultipartFile> files = Collections.singletonList(file);

			// when & then
			assertThatThrownBy(() -> imageUploadUseCase.uploadToTemp(files))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("파일명이 null입니다");

			then(imageValidator).should().validate(eq(file));
		}
	}
}