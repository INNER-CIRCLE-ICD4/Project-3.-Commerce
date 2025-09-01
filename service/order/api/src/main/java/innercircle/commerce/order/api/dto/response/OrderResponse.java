package innercircle.commerce.order.api.dto.response;

import innercircle.commerce.order.application.port.in.result.OrderResult;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderResponse
 * 주문 응답 DTO
 */
@Getter
@Setter
public class OrderResponse {
    
    private Long orderId;
    
    private String orderNumber;
    
    private Long memberId;
    
    private BigDecimal totalAmount;
    
    private String status;
    
    private ShippingAddressResponse shippingAddress;
    
    private List<OrderItemResponse> orderItems;
    
    private LocalDateTime orderedAt;
    
    /**
     * OrderResult로부터 OrderResponse 생성
     */
    public static OrderResponse from(OrderResult result) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(result.orderId());
        response.setOrderNumber(result.orderNumber());
        response.setMemberId(result.memberId());
        response.setTotalAmount(result.totalAmount());
        response.setStatus(result.status().name());
        response.setShippingAddress(ShippingAddressResponse.from(result.shippingAddress()));
        response.setOrderItems(result.orderItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList()));
        response.setOrderedAt(result.orderedAt());
        return response;
    }

    /**
     * ShippingAddressResponse
     * 배송 주소 응답 DTO
     */
    @Getter
    @Setter
    public static class ShippingAddressResponse {
        
        private String recipientName;
        
        private String phoneNumber;
        
        private String fullAddress;
        
        public static ShippingAddressResponse from(OrderResult.ShippingAddressResult result) {
            ShippingAddressResponse response = new ShippingAddressResponse();
            response.setRecipientName(result.recipientName());
            response.setPhoneNumber(result.phoneNumber());
            response.setFullAddress(result.fullAddress());
            return response;
        }
    }
    
    /**
     * OrderItemResponse
     * 주문 항목 응답 DTO
     */
    @Getter
    @Setter
    public static class OrderItemResponse {
        
        private Long orderItemId;
        
        private Long productId;
        
        private String productName;
        
        private BigDecimal unitPrice;
        
        private Integer quantity;
        
        private BigDecimal subtotal;
        
        private String status;
        
        public static OrderItemResponse from(OrderResult.OrderItemResult result) {
            OrderItemResponse response = new OrderItemResponse();
            response.setOrderItemId(result.orderItemId());
            response.setProductId(result.productId());
            response.setProductName(result.productName());
            response.setUnitPrice(result.unitPrice());
            response.setQuantity(result.quantity());
            response.setSubtotal(result.subtotal());
            response.setStatus(result.status());
            return response;
        }

    }
}
