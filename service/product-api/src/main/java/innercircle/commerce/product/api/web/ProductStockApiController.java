package innercircle.commerce.product.api.web;

import innercircle.commerce.product.api.application.ProductInventoryFacade;
import innercircle.commerce.product.api.web.dto.ApiResponse;
import innercircle.commerce.product.api.web.dto.ProductStockUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 상품 재고 API 컨트롤러
 * 
 * 시스템 간 연동을 위한 재고 증감 API를 제공합니다.
 *
 * @author 황인웅
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductStockApiController {

	private final ProductInventoryFacade productInventoryFacade;

	/**
	 * 상품 재고를 증가시킵니다.
	 *
	 * @param productId 상품 ID
	 * @param request 재고 증가 요청
	 * @return 성공 응답
	 */
	@PatchMapping("/{productId}/increase-stock")
	public ResponseEntity<ApiResponse<Void>> increaseStock(
			@PathVariable Long productId,
			@Valid @RequestBody ProductStockUpdateRequest request
	) throws InterruptedException {
		log.info("상품 재고 증가 요청 - ProductId: {}, Quantity: {}", productId, request.getQuantity());
		
		productInventoryFacade.updateStockWithRetry(request.toIncreaseCommand(productId));
		
		log.info("상품 재고 증가 완료 - ProductId: {}, Quantity: {}", productId, request.getQuantity());
		return ResponseEntity.ok(ApiResponse.success());
	}

	/**
	 * 상품 재고를 감소시킵니다.
	 *
	 * @param productId 상품 ID
	 * @param request 재고 감소 요청
	 * @return 성공 응답
	 */
	@PatchMapping("/{productId}/decrease-stock")
	public ResponseEntity<ApiResponse<Void>> decreaseStock(
			@PathVariable Long productId,
			@Valid @RequestBody ProductStockUpdateRequest request
	) throws InterruptedException {
		log.info("상품 재고 감소 요청 - ProductId: {}, Quantity: {}", productId, request.getQuantity());
		
		productInventoryFacade.updateStockWithRetry(request.toDecreaseCommand(productId));
		
		log.info("상품 재고 감소 완료 - ProductId: {}, Quantity: {}", productId, request.getQuantity());
		return ResponseEntity.ok(ApiResponse.success());
	}
}