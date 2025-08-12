package innercircle.commerce.product.admin.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import innercircle.commerce.product.admin.application.ImageUploadUseCase;
import innercircle.commerce.product.admin.application.ProductCreateUseCase;
import innercircle.commerce.product.admin.application.dto.ImageUploadInfo;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.InvalidBrandException;
import innercircle.commerce.product.admin.application.exception.NotFoundTempImageException;
import innercircle.commerce.product.admin.fixtures.ProductCreateRequestFixtures;
import innercircle.commerce.product.admin.fixtures.ProductFixtures;
import innercircle.commerce.product.admin.web.dto.ProductCreateRequest;
import innercircle.commerce.product.core.domain.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("상품 컨트롤러 테스트")
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ProductCreateUseCase productCreateUseCase;

	@MockBean
	private ImageUploadUseCase imageUploadUseCase;

	@Nested
	@DisplayName("상품 등록")
	class CreateProduct {

		@Test
		@DisplayName("유효한 요청으로 상품을 정상적으로 등록할 수 있다.")
		void 상품_등록_성공() throws Exception {
			// given
			ProductCreateRequest request = ProductCreateRequestFixtures.createValidRequest();
			Product mockProduct = ProductFixtures.createSavedProduct();
			
			given(productCreateUseCase.create(any())).willReturn(mockProduct);

			// when & then
			mockMvc.perform(post("/api/admin/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data").exists())
					.andExpect(jsonPath("$.data.id").exists())
					.andExpect(jsonPath("$.data.name").value(mockProduct.getName()))
					.andExpect(jsonPath("$.data.brandId").value(mockProduct.getBrandId()))
					.andExpect(jsonPath("$.data.leafCategoryId").value(mockProduct.getLeafCategoryId()))
					.andExpect(jsonPath("$.data.basePrice").value(mockProduct.getBasePrice()))
					.andExpect(jsonPath("$.data.stock").value(mockProduct.getStock()))
					.andExpect(jsonPath("$.data.status").value(mockProduct.getStatus().toString()))
					.andExpect(jsonPath("$.data.saleType").value(mockProduct.getSaleType().toString()))
					.andExpect(jsonPath("$.error").doesNotExist())
					.andExpect(jsonPath("$.timestamp").exists());

			verify(productCreateUseCase).create(any());
		}

		@Test
		@DisplayName("상품명이 없는 경우 400 Bad Request를 반환한다.")
		void 상품명_없음_검증_실패() throws Exception {
			// given
			ProductCreateRequest request = ProductCreateRequestFixtures.createInvalidRequestWithoutName();

			// when & then
			mockMvc.perform(post("/api/admin/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest());

			verify(productCreateUseCase, never()).create(any());
		}

		@Test
		@DisplayName("이미지가 없는 경우 400 Bad Request를 반환한다.")
		void 이미지_없음_검증_실패() throws Exception {
			// given
			ProductCreateRequest request = ProductCreateRequestFixtures.createInvalidRequestWithoutImages();

			// when & then
			mockMvc.perform(post("/api/admin/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest());

			verify(productCreateUseCase, never()).create(any());
		}

		@Test
		@DisplayName("가격이 음수인 경우 400 Bad Request를 반환한다.")
		void 음수_가격_검증_실패() throws Exception {
			// given
			ProductCreateRequest request = ProductCreateRequestFixtures.createInvalidRequestWithNegativePrice();

			// when & then
			mockMvc.perform(post("/api/admin/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest());

			verify(productCreateUseCase, never()).create(any());
		}

		@Test
		@DisplayName("JSON 형식이 잘못된 경우 400 Bad Request를 반환한다.")
		void 잘못된_JSON_형식() throws Exception {
			// given
			String invalidJson = "{ invalid json }";

			// when & then
			mockMvc.perform(post("/api/admin/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(invalidJson))
					.andExpect(status().isBadRequest());

			verify(productCreateUseCase, never()).create(any());
		}

		@Test
		@DisplayName("상품명이 중복된 경우 적절한 에러 응답을 반환한다.")
		void 상품명_중복_예외_처리() throws Exception {
			// given
			ProductCreateRequest request = ProductCreateRequestFixtures.createValidRequest();
			given(productCreateUseCase.create(any())).willThrow(new DuplicateProductNameException(request.name()));

			// when & then
			mockMvc.perform(post("/api/admin/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.success").value(false))
					.andExpect(jsonPath("$.data").doesNotExist())
					.andExpect(jsonPath("$.error").exists())
					.andExpect(jsonPath("$.error.code").value("PRODUCT_901"))
					.andExpect(jsonPath("$.timestamp").exists());

			verify(productCreateUseCase).create(any());
		}

		@Test
		@DisplayName("존재하지 않는 브랜드인 경우 적절한 에러 응답을 반환한다.")
		void 브랜드_존재하지_않음_예외_처리() throws Exception {
			// given
			ProductCreateRequest request = ProductCreateRequestFixtures.createValidRequest();
			given(productCreateUseCase.create(any())).willThrow(new InvalidBrandException(request.brandId()));

			// when & then
			mockMvc.perform(post("/api/admin/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.success").value(false))
					.andExpect(jsonPath("$.error.code").value("PRODUCT_001"));

			verify(productCreateUseCase).create(any());
		}

		@Test
		@DisplayName("임시 이미지를 찾을 수 없는 경우 적절한 에러 응답을 반환한다.")
		void 임시_이미지_찾을수없음_예외_처리() throws Exception {
			// given
			ProductCreateRequest request = ProductCreateRequestFixtures.createValidRequest();
			given(productCreateUseCase.create(any())).willThrow(new NotFoundTempImageException("1234"));

			// when & then
			mockMvc.perform(post("/api/admin/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.success").value(false))
					.andExpect(jsonPath("$.error.code").value("PRODUCT_402"));

			verify(productCreateUseCase).create(any());
		}
	}

	@Nested
	@DisplayName("임시 이미지 업로드")
	class TempImageUpload {

		@Test
		@DisplayName("이미지 파일들을 정상적으로 임시 업로드할 수 있다.")
		void 임시_이미지_업로드_성공() throws Exception {
			// given
			MockMultipartFile file1 = new MockMultipartFile(
					"files", "image1.jpg", "image/jpeg", "image1 content".getBytes());
			MockMultipartFile file2 = new MockMultipartFile(
					"files", "image2.jpg", "image/jpeg", "image2 content".getBytes());

			List<ImageUploadInfo> mockUploadInfos = List.of(
					new ImageUploadInfo(1L, "image1.jpg", "https://s3.amazonaws.com/bucket/temp/1.jpg"),
					new ImageUploadInfo(2L, "image2.jpg", "https://s3.amazonaws.com/bucket/temp/2.jpg")
			);

			given(imageUploadUseCase.uploadToTemp(any())).willReturn(mockUploadInfos);

			// when & then
			mockMvc.perform(multipart("/api/admin/products/images/temp-upload")
					.file(file1)
					.file(file2)
					.contentType(MediaType.MULTIPART_FORM_DATA))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data").isArray())
					.andExpect(jsonPath("$.data.length()").value(2))
					.andExpect(jsonPath("$.data[0].id").value(1L))
					.andExpect(jsonPath("$.data[0].originalName").value("image1.jpg"))
					.andExpect(jsonPath("$.data[0].url").exists())
					.andExpect(jsonPath("$.data[1].id").value(2L))
					.andExpect(jsonPath("$.data[1].originalName").value("image2.jpg"))
					.andExpect(jsonPath("$.data[1].url").exists())
					.andExpect(jsonPath("$.error").doesNotExist());

			verify(imageUploadUseCase).uploadToTemp(any());
		}

		@Test
		@DisplayName("파일이 없는 경우 400 Bad Request를 반환한다.")
		void 파일_없음_검증_실패() throws Exception {
			// when & then
			mockMvc.perform(multipart("/api/admin/products/images/temp-upload")
					.contentType(MediaType.MULTIPART_FORM_DATA))
					.andExpect(status().isBadRequest());

			verify(imageUploadUseCase, never()).uploadToTemp(any());
		}
	}
}