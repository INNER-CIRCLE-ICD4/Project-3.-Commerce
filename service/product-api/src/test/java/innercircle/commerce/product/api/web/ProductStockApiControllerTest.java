package innercircle.commerce.product.api.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import innercircle.commerce.product.api.application.ProductInventoryFacade;
import innercircle.commerce.product.api.application.exception.ProductNotFoundException;
import innercircle.commerce.product.api.application.exception.StockConflictException;
import innercircle.commerce.product.api.web.dto.ProductStockUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductStockApiController.class)
class ProductStockApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProductInventoryFacade productInventoryFacade;

	@Nested
	@DisplayName("재고 증가 API")
	class IncreaseStockApi {

		@Test
		@DisplayName("유효한 요청으로 재고 증가에 성공한다.")
		void 재고_증가_성공() throws Exception {
			// given
			Long productId = 1L;
			ProductStockUpdateRequest request = new ProductStockUpdateRequest(10);

			willDoNothing().given(productInventoryFacade).updateStockWithRetry(any());

			// when & then
			mockMvc.perform(patch("/api/v1/products/{productId}/increase-stock", productId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data").doesNotExist());
		}

		@Test
		@DisplayName("수량이 null이면 유효성 검사 오류가 발생한다.")
		void 재고_증가_수량_null_검증_오류() throws Exception {
			// given
			Long productId = 1L;
			ProductStockUpdateRequest request = new ProductStockUpdateRequest(null);

			// when & then
			mockMvc.perform(patch("/api/v1/products/{productId}/increase-stock", productId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("수량이 0 이하면 유효성 검사 오류가 발생한다.")
		void 재고_증가_수량_0이하_검증_오류() throws Exception {
			// given
			Long productId = 1L;
			ProductStockUpdateRequest request = new ProductStockUpdateRequest(0);

			// when & then
			mockMvc.perform(patch("/api/v1/products/{productId}/increase-stock", productId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("존재하지 않는 상품 ID로 요청하면 404 오류가 발생한다.")
		void 재고_증가_상품_없음_오류() throws Exception {
			// given
			Long productId = 999L;
			ProductStockUpdateRequest request = new ProductStockUpdateRequest(10);

			willThrow(new ProductNotFoundException(productId)).given(productInventoryFacade).updateStockWithRetry(any());

			// when & then
			mockMvc.perform(patch("/api/v1/products/{productId}/increase-stock", productId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("동시성 충돌로 인한 재시도 실패 시 409 오류가 발생한다.")
		void 재고_증가_동시성_충돌_오류() throws Exception {
			// given
			Long productId = 1L;
			ProductStockUpdateRequest request = new ProductStockUpdateRequest(5);

			willThrow(new StockConflictException("재고 변경 중 충돌이 발생했습니다."))
					.given(productInventoryFacade).updateStockWithRetry(any());

			// when & then
			mockMvc.perform(patch("/api/v1/products/{productId}/increase-stock", productId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isConflict());
		}
	}

	@Nested
	@DisplayName("재고 감소 API")
	class DecreaseStockApi {

		@Test
		@DisplayName("유효한 요청으로 재고 감소에 성공한다.")
		void 재고_감소_성공() throws Exception {
			// given
			Long productId = 1L;
			ProductStockUpdateRequest request = new ProductStockUpdateRequest(5);

			willDoNothing().given(productInventoryFacade).updateStockWithRetry(any());

			// when & then
			mockMvc.perform(patch("/api/v1/products/{productId}/decrease-stock", productId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data").doesNotExist());
		}

		@Test
		@DisplayName("재고 부족으로 감소에 실패하면 400 오류가 발생한다.")
		void 재고_감소_부족_오류() throws Exception {
			// given
			Long productId = 1L;
			ProductStockUpdateRequest request = new ProductStockUpdateRequest(10);

			willThrow(new IllegalArgumentException("재고가 부족합니다. 현재 재고: 5, 요청 수량: 10"))
					.given(productInventoryFacade).updateStockWithRetry(any());

			// when & then
			mockMvc.perform(patch("/api/v1/products/{productId}/decrease-stock", productId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest());
		}
	}
}