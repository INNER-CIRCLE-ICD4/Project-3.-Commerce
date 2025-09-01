package innercircle.commerce.order.api.controller;

import innercircle.commerce.order.api.dto.request.PlaceOrderRequest;
import innercircle.commerce.order.api.dto.response.OrderResponse;
import innercircle.commerce.order.application.port.in.CancelOrderUseCase;
import innercircle.commerce.order.application.port.in.GetOrderUseCase;
import innercircle.commerce.order.application.port.in.PlaceOrderUseCase;
import innercircle.commerce.order.application.port.in.command.PlaceOrderCommand;
import innercircle.commerce.order.application.port.in.result.OrderResult;
import innercircle.commerce.order.domain.model.aggregate.Order;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OrderController
 * 주문 관련 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    /**
     * 주문 생성 및 즉시 결제
     */
    @PostMapping("add")
    public ResponseEntity<OrderResult> createOrder(
            @RequestBody PlaceOrderRequest request
    ) {
        try {
            PlaceOrderCommand command = toCommand(request);
            OrderResult order = placeOrderUseCase.placeOrder(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (Exception e) {
            log.error("Failed to create order", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 주문 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        try {
            Order order = getOrderUseCase.getOrder(orderId);
            OrderResult orderResult = OrderResult.from(order);
            OrderResponse response = OrderResponse.from(orderResult);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 회원별 주문 목록 조회
     */
    @GetMapping("/members/{memberId}")
    public ResponseEntity<List<OrderResponse>> getMemberOrders(@PathVariable Long memberId) {
        List<Order> orders = getOrderUseCase.getMemberOrders(memberId);
        List<OrderResponse> responses = orders.stream()
                .map(order -> OrderResponse.from(OrderResult.from(order)))
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 전체 주문 취소
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelEntireOrder(
            @PathVariable Long orderId,
            @RequestBody CancelOrderRequest request
    ) {
        try {
            cancelOrderUseCase.cancelEntireOrder(orderId, request.getReason());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to cancel order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 개별 상품 취소
     */
    @PostMapping("/{orderId}/items/{orderItemId}/cancel")
    public ResponseEntity<Void> cancelOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long orderItemId,
            @RequestBody CancelOrderRequest request
    ) {
        try {
            cancelOrderUseCase.cancelOrderItem(orderId, orderItemId, request.getReason());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to cancel order item: {} from order: {}", orderItemId, orderId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    public static PlaceOrderCommand toCommand(PlaceOrderRequest request) {
        return new PlaceOrderCommand(
                request.getMemberId(),
                new PlaceOrderCommand.ShippingInfo(
                        request.getRecipientName(),
                        request.getPhoneNumber(),
                        request.getAddressCode(),
                        request.getAddress(),
                        request.getAddressDetail(),
                        request.getDeliveryRequest()
                ),
                request.getOrderItems().stream()
                        .map(OrderController::mapToOrderItemCommand)
                        .toList(),
                PlaceOrderCommand.PaymentMethodType.valueOf(request.getPaymentMethod()) // enum 매핑
        );
    }

    private static PlaceOrderCommand.OrderItemCommand mapToOrderItemCommand(
            PlaceOrderRequest.PlaceOrderItemRequest item
    ) {
        return new PlaceOrderCommand.OrderItemCommand(
                item.getProductId(),
                item.getProductOptionId(),
                item.getQuantity()
        );
    }

    @Getter
    @Setter
    public static class CancelOrderRequest {
        private String reason;
    }

}
