package innercircle.commerce.order.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * PlaceOrderRequest
 * 주문 생성 요청 DTO
 */
@Getter
@Setter
public class PlaceOrderRequest {
    
    @JsonProperty("memberId")
    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;
    
    @JsonProperty("recipientName")
    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String recipientName;
    
    @JsonProperty("phoneNumber")
    @NotBlank(message = "전화번호는 필수입니다.")
    private String phoneNumber;
    
    @JsonProperty("addressCode")
    @NotBlank(message = "주소 코드는 필수입니다.")
    private String addressCode;
    
    @JsonProperty("address")
    @NotBlank(message = "주소는 필수입니다.")
    private String address;
    
    @JsonProperty("addressDetail")
    @NotBlank(message = "상세 주소는 필수입니다.")
    private String addressDetail;
    
    @JsonProperty("paymentMethod")
    @NotBlank(message = "결제 방법은 필수입니다.")
    private String paymentMethod;
    
    @JsonProperty("deliveryRequest")
    private String deliveryRequest;
    
    @JsonProperty("orderItems")
    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<PlaceOrderItemRequest> orderItems;
    
    public PlaceOrderRequest() {}
    
    /**
     * PlaceOrderItemRequest
     * 문 항목 요청 DTO
     */
    @Getter
    @Setter
    public static class PlaceOrderItemRequest {
        
        @JsonProperty("productId")
        @NotNull(message = "상품 ID는 필수입니다.")
        private Long productId;
        
        @JsonProperty("productOptionId")
        @NotNull(message = "상품 옵션 ID는 필수입니다.")
        private Long productOptionId;
        
        @JsonProperty("quantity")
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
        private Integer quantity;
        
        public PlaceOrderItemRequest() {}
    }
}
