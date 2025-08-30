package innercircle.commerce.product.admin.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import innercircle.commerce.product.admin.application.*;
import innercircle.commerce.product.admin.application.dto.ImageUploadInfo;
import innercircle.commerce.product.admin.application.exception.DuplicateProductNameException;
import innercircle.commerce.product.admin.application.exception.InvalidBrandException;
import innercircle.commerce.product.admin.application.exception.NotFoundTempImageException;
import innercircle.commerce.product.admin.application.exception.ProductNotFoundException;
import innercircle.commerce.product.admin.application.exception.StockConflictException;
import innercircle.commerce.product.admin.fixtures.ProductCreateRequestFixtures;
import innercircle.commerce.product.admin.application.dto.ProductAdminInfo;
import innercircle.commerce.product.admin.application.dto.ProductListAdminInfo;
import innercircle.commerce.product.admin.fixtures.ProductFixtures;
import innercircle.commerce.product.admin.web.dto.ProductCreateRequest;
import innercircle.commerce.product.core.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ProductController.class})
@DisplayName("상품 컨트롤러 테스트")
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProductCreateUseCase productCreateUseCase;

	@MockitoBean
	private ProductUpdateUseCase productUpdateUseCase;

	@MockitoBean
	private ProductDeleteUseCase productDeleteUseCase;

	@MockitoBean
	private ProductRetrieveUseCase productRetrieveUseCase;

	@MockitoBean
	private ImageUploadUseCase imageUploadUseCase;

	@MockitoBean
	private ProductInventoryUpdateUseCase productInventoryUpdateUseCase;

	@Nested
	@DisplayName("상품 등록")
	class CreateProduct {

		@Test
		@DisplayName("유효한 요청으로 상품을 정상적으로 등록할 수 있다.")
		void 상품_등록_성공 () throws Exception {
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
				   .andExpect(jsonPath("$.data.categoryId").value(mockProduct.getCategoryId()))
				   .andExpect(jsonPath("$.data.price").value(mockProduct.getPrice()))
				   .andExpect(jsonPath("$.data.stock").value(mockProduct.getStock()))
				   .andExpect(jsonPath("$.data.status").value(mockProduct.getStatus().toString()))
				   .andExpect(jsonPath("$.data.saleType").value(mockProduct.getSaleType().toString()))
				   .andExpect(jsonPath("$.error").doesNotExist())
				   .andExpect(jsonPath("$.timestamp").exists());

			verify(productCreateUseCase).create(any());
		}

		@Test
		@DisplayName("상품명이 없는 경우 400 Bad Request를 반환한다.")
		void 상품명_없음_검증_실패 () throws Exception {
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
		void 이미지_없음_검증_실패 () throws Exception {
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
		void 음수_가격_검증_실패 () throws Exception {
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
		@DisplayName("상품명이 중복된 경우 적절한 에러 응답을 반환한다.")
		void 상품명_중복_예외_처리 () throws Exception {
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
		void 브랜드_존재하지_않음_예외_처리 () throws Exception {
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
		void 임시_이미지_찾을수없음_예외_처리 () throws Exception {
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
		void 임시_이미지_업로드_성공 () throws Exception {
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
	}

	@Nested
	@DisplayName("상품 수정")
	class UpdateProduct {

		@Test
		@DisplayName("상품 기본 정보를 정상적으로 수정할 수 있다.")
		void 상품_기본정보_수정_성공() throws Exception {
			// given
			Long productId = 1L;
			var updateRequest = """
				{
					"name": "수정된 상품명",
					"basePrice": 25000,
					"detailContent": "수정된 상품 설명"
				}
				""";
			
			Product updatedProduct = ProductFixtures.createValidProduct();
			given(productUpdateUseCase.updateProduct(any())).willReturn(updatedProduct);

			// when & then
			mockMvc.perform(patch("/api/admin/products/{id}", productId)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(updateRequest))
				   .andExpect(status().isOk())
				   .andExpect(jsonPath("$.success").value(true))
				   .andExpect(jsonPath("$.data.id").exists())
				   .andExpect(jsonPath("$.data.name").exists())
				   .andExpect(jsonPath("$.error").doesNotExist());

			verify(productUpdateUseCase).updateProduct(any());
		}

		@Test
		@DisplayName("존재하지 않는 상품 수정 시 404 오류가 발생한다.")
		void 존재하지_않는_상품_수정_실패() throws Exception {
			// given
			Long productId = 999L;
			var updateRequest = """
				{
					"name": "수정된 상품명",
					"basePrice": 25000,
					"detailContent": "수정된 상품 설명"
				}
				""";
			
			given(productUpdateUseCase.updateProduct(any()))
				.willThrow(new ProductNotFoundException(productId));

			// when & then
			mockMvc.perform(patch("/api/admin/products/{id}", productId)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(updateRequest))
				   .andExpect(status().isNotFound());

			verify(productUpdateUseCase).updateProduct(any());
		}


		@Test
		@DisplayName("상품 정보와 이미지 삭제를 함께 수행할 수 있다.")
		void 상품_정보_및_이미지_삭제_성공() throws Exception {
			// given
			Long productId = 1L;
			var updateRequest = """
				{
					"name": "수정된 상품명",
					"basePrice": 25000,
					"detailContent": "수정된 상품 설명",
					"imagesToDelete": [
						"https://s3.amazonaws.com/commerce/products/1/image1.jpg"
					]
				}
				""";
			
			Product updatedProduct = ProductFixtures.createValidProduct();
			given(productUpdateUseCase.updateProduct(any())).willReturn(updatedProduct);

			// when & then
			mockMvc.perform(patch("/api/admin/products/{id}", productId)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(updateRequest))
				   .andExpect(status().isOk())
				   .andExpect(jsonPath("$.success").value(true))
				   .andExpect(jsonPath("$.data").exists())
				   .andExpect(jsonPath("$.error").doesNotExist());

			verify(productUpdateUseCase).updateProduct(any());
		}

		@Test
		@DisplayName("상품 정보와 이미지 추가를 함께 수행할 수 있다.")
		void 상품_정보_및_이미지_추가_성공() throws Exception {
			// given
			Long productId = 1L;
			var updateRequest = """
				{
					"name": "수정된 상품명",
					"basePrice": 25000,
					"detailContent": "수정된 상품 설명",
					"imagesToAdd": [
						{
							"id": 1,
							"url": "https://s3.amazonaws.com/temp/1.jpg",
							"originalName": "image1.jpg",
							"sortOrder": 2
						}
					]
				}
				""";
			
			Product updatedProduct = ProductFixtures.createValidProduct();
			given(productUpdateUseCase.updateProduct(any())).willReturn(updatedProduct);

			// when & then
			mockMvc.perform(patch("/api/admin/products/{id}", productId)
					   .contentType(MediaType.APPLICATION_JSON)
					   .content(updateRequest))
				   .andExpect(status().isOk())
				   .andExpect(jsonPath("$.success").value(true))
				   .andExpect(jsonPath("$.data").exists())
				   .andExpect(jsonPath("$.error").doesNotExist());

			verify(productUpdateUseCase).updateProduct(any());
		}
	}

	@Nested
	@DisplayName("상품 삭제")
	class DeleteProduct {

		@Test
		@DisplayName("상품을 정상적으로 삭제할 수 있다.")
		void 상품_삭제_성공() throws Exception {
			// given
			Long productId = 1L;

			// when & then
			mockMvc.perform(delete("/api/admin/products/{id}", productId))
				   .andExpect(status().isNoContent());

			verify(productDeleteUseCase).deleteProduct(productId);
		}

		@Test
		@DisplayName("존재하지 않는 상품 삭제 시 404 에러를 반환한다.")
		void 존재하지_않는_상품_삭제_시_404_에러() throws Exception {
			// given
			Long nonExistentProductId = 999L;

			doThrow(new ProductNotFoundException(nonExistentProductId))
					.when(productDeleteUseCase).deleteProduct(nonExistentProductId);

			// when & then
			mockMvc.perform(delete("/api/admin/products/{id}", nonExistentProductId))
				   .andExpect(status().isNotFound());

			verify(productDeleteUseCase).deleteProduct(nonExistentProductId);
		}

		@Test
		@DisplayName("이미 삭제된 상품을 다시 삭제하면 400 에러를 반환한다.")
		void 이미_삭제된_상품_삭제_시_400_에러() throws Exception {
			// given
			Long alreadyDeletedProductId = 1L;

			doThrow(new IllegalArgumentException("이미 삭제된 상품입니다."))
					.when(productDeleteUseCase).deleteProduct(alreadyDeletedProductId);

			// when & then
			mockMvc.perform(delete("/api/admin/products/{id}", alreadyDeletedProductId))
				   .andExpect(status().isBadRequest());

			verify(productDeleteUseCase).deleteProduct(alreadyDeletedProductId);
		}
	}

	@Nested
	@DisplayName("상품 조회")
	class RetrieveProduct {

		@Test
		@DisplayName("상품 목록을 조회할 수 있다.")
		void 상품_목록_조회_성공() throws Exception {
			// given
			List<ProductListAdminInfo> productInfos = List.of(
					ProductListAdminInfo.from(ProductFixtures.createValidProduct()),
					ProductListAdminInfo.from(ProductFixtures.createValidProduct())
			);
			Page<ProductListAdminInfo> productPage = new PageImpl<>(productInfos, PageRequest.of(0, 20), productInfos.size());

			given(productRetrieveUseCase.getProducts(any()))
					.willReturn(productPage);

			// when & then
			mockMvc.perform(get("/api/admin/products")
							.param("page", "0")
							.param("size", "20"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.content").isArray())
					.andExpect(jsonPath("$.data.content.length()").value(2))
					.andExpect(jsonPath("$.data.totalElements").value(2))
					.andExpect(jsonPath("$.data.content[0].id").exists())
					.andExpect(jsonPath("$.data.content[0].name").exists());

			verify(productRetrieveUseCase).getProducts(any());
		}

		@Test
		@DisplayName("상태별 상품 목록을 조회할 수 있다.")
		void 상태별_상품_목록_조회_성공() throws Exception {
			// given
			List<ProductListAdminInfo> productInfos = List.of(ProductListAdminInfo.from(ProductFixtures.createValidProduct()));
			Page<ProductListAdminInfo> productPage = new PageImpl<>(productInfos, PageRequest.of(0, 20), productInfos.size());

			given(productRetrieveUseCase.getProducts(any()))
					.willReturn(productPage);

			// when & then
			mockMvc.perform(get("/api/admin/products")
							.param("status", "SALE")
							.param("page", "0")
							.param("size", "20"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.content").isArray())
					.andExpect(jsonPath("$.data.content.length()").value(1));

			verify(productRetrieveUseCase).getProducts(any());
		}

		@Test
		@DisplayName("카테고리별 상품 목록을 조회할 수 있다.")
		void 카테고리별_상품_목록_조회_성공() throws Exception {
			// given
			List<ProductListAdminInfo> productInfos = List.of(ProductListAdminInfo.from(ProductFixtures.createValidProduct()));
			Page<ProductListAdminInfo> productPage = new PageImpl<>(productInfos, PageRequest.of(0, 20), productInfos.size());

			given(productRetrieveUseCase.getProducts(any()))
					.willReturn(productPage);

			// when & then
			mockMvc.perform(get("/api/admin/products")
							.param("categoryId", "1")
							.param("page", "0")
							.param("size", "20"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.content").isArray())
					.andExpect(jsonPath("$.data.content.length()").value(1));

			verify(productRetrieveUseCase).getProducts(any());
		}

		@Test
		@DisplayName("상품 상세 정보를 조회할 수 있다.")
		void 상품_상세_조회_성공() throws Exception {
			// given
			Long productId = 1L;
			ProductAdminInfo productInfo = ProductAdminInfo.from(ProductFixtures.createValidProduct());

			given(productRetrieveUseCase.getProduct(productId))
					.willReturn(productInfo);

			// when & then
			mockMvc.perform(get("/api/admin/products/{id}", productId))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.id").exists())
					.andExpect(jsonPath("$.data.name").exists())
					.andExpect(jsonPath("$.data.categoryId").exists())
					.andExpect(jsonPath("$.data.brandId").exists())
					.andExpect(jsonPath("$.data.price").exists())
					.andExpect(jsonPath("$.data.status").exists())
					.andExpect(jsonPath("$.data.images").isArray());

			verify(productRetrieveUseCase).getProduct(productId);
		}

		@Test
		@DisplayName("존재하지 않는 상품 조회 시 404 에러를 반환한다.")
		void 존재하지_않는_상품_조회_시_404_에러() throws Exception {
			// given
			Long nonExistentProductId = 999L;

			given(productRetrieveUseCase.getProduct(nonExistentProductId))
					.willThrow(new ProductNotFoundException(nonExistentProductId));

			// when & then
			mockMvc.perform(get("/api/admin/products/{id}", nonExistentProductId))
					.andExpect(status().isNotFound());

			verify(productRetrieveUseCase).getProduct(nonExistentProductId);
		}
	}

	@Nested
	@DisplayName("상품 재고 조정")
	class UpdateInventory {

		@Test
		@DisplayName("재고를 정상적으로 증가시킬 수 있다.")
		void 재고_증가_성공() throws Exception {
			// given
			Product updatedProduct = ProductFixtures.createUpdatedProduct();
			String requestBody = """
					{
						"operationType": "INCREASE",
						"quantity": 10
					}
					""";

			given(productInventoryUpdateUseCase.updateStock(any()))
					.willReturn(updatedProduct);

			// when & then
			mockMvc.perform(patch("/api/admin/products/{id}/inventory", updatedProduct.getId())
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.productId").value(updatedProduct.getId()))
					.andExpect(jsonPath("$.data.updatedStock").exists())
					.andExpect(jsonPath("$.data.version").exists());

			verify(productInventoryUpdateUseCase).updateStock(any());
		}

		@Test
		@DisplayName("재고를 정상적으로 감소시킬 수 있다.")
		void 재고_감소_성공() throws Exception {
			// given
			Product updatedProduct = ProductFixtures.createUpdatedProduct();
			String requestBody = """
					{
						"operationType": "DECREASE",
						"quantity": 5
					}
					""";

			given(productInventoryUpdateUseCase.updateStock(any()))
					.willReturn(updatedProduct);

			// when & then
			mockMvc.perform(patch("/api/admin/products/{id}/inventory", updatedProduct.getId())
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.productId").value(updatedProduct.getId()))
					.andExpect(jsonPath("$.data.updatedStock").exists());

			verify(productInventoryUpdateUseCase).updateStock(any());
		}

		@Test
		@DisplayName("존재하지 않는 상품 ID로 재고 조정 시 404 에러를 반환한다.")
		void 존재하지_않는_상품_재고조정_404_에러() throws Exception {
			// given
			Long nonExistentProductId = 999L;
			String requestBody = """
					{
						"operationType": "INCREASE",
						"quantity": 10
					}
					""";

			given(productInventoryUpdateUseCase.updateStock(any()))
					.willThrow(new ProductNotFoundException(nonExistentProductId));

			// when & then
			mockMvc.perform(patch("/api/admin/products/{id}/inventory", nonExistentProductId)
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isNotFound());

			verify(productInventoryUpdateUseCase).updateStock(any());
		}

		@Test
		@DisplayName("재고 충돌 발생 시 409 에러를 반환한다.")
		void 재고_충돌_409_에러() throws Exception {
			// given
			Long productId = 1L;
			String requestBody = """
					{
						"operationType": "INCREASE",
						"quantity": 10
					}
					""";

			given(productInventoryUpdateUseCase.updateStock(any()))
					.willThrow(new StockConflictException("재고 변경 중 충돌이 발생했습니다."));

			// when & then
			mockMvc.perform(patch("/api/admin/products/{id}/inventory", productId)
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isConflict());

			verify(productInventoryUpdateUseCase).updateStock(any());
		}

		@Test
		@DisplayName("음수 수량으로 요청 시 400 에러를 반환한다.")
		void 음수_수량_400_에러() throws Exception {
			// given
			Long productId = 1L;
			String requestBody = """
					{
						"operationType": "INCREASE",
						"quantity": -5
					}
					""";

			// when & then
			mockMvc.perform(patch("/api/admin/products/{id}/inventory", productId)
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("필수 필드 누락 시 400 에러를 반환한다.")
		void 필수_필드_누락_400_에러() throws Exception {
			// given
			Long productId = 1L;
			String requestBody = """
					{
						"quantity": 10
					}
					""";

			// when & then
			mockMvc.perform(patch("/api/admin/products/{id}/inventory", productId)
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isBadRequest());
		}
	}
}