package innercircle.commerce.order.application.port.in.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * PlaceOrderCommand
 * 주문 생성 명령 객체
 */
public record PlaceOrderCommand(
        @NotNull Long memberId,
        @NotNull ShippingInfo shippingInfo,
        @NotEmpty List<OrderItemCommand> orderItems,
        @NotNull PaymentMethodType paymentMethod
) {
    public record ShippingInfo(
            @NotBlank String recipientName,
            @NotBlank String phoneNumber,
            @NotBlank String addressCode,
            @NotBlank String address,
            @NotBlank String addressDetail,
            String deliveryRequest
    ) {}

    public record OrderItemCommand(
            @NotNull Long productId,
            @NotNull Long productOptionId,
            @NotNull @Min(1) Integer quantity
    ) {}

    public enum PaymentMethodType {
        CREDIT_CARD, BANK_TRANSFER, VIRTUAL_ACCOUNT, MOBILE_PAYMENT
    }
}
