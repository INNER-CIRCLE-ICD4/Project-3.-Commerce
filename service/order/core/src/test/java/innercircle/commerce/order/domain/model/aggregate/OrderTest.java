package innercircle.commerce.order.domain.model.aggregate;

import innercircle.commerce.order.domain.model.entity.OrderItem;
import innercircle.commerce.order.domain.model.entity.OrderPayment;
import innercircle.commerce.order.domain.model.vo.*;
import innercircle.commerce.order.domain.model.vo.enums.OrderItemStatus;
import innercircle.commerce.order.domain.model.vo.enums.OrderStatus;
import innercircle.commerce.order.domain.model.vo.enums.PaymentMethodType;
import innercircle.commerce.order.domain.services.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Order 도메인 모델 테스트
 */
class OrderTest {

    private MemberId memberId;
    private ShippingAddress shippingAddress;
    private List<OrderItem> orderItems;
    private IdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        // Mock IdGenerator
        idGenerator = new TestIdGenerator();

        memberId = MemberId.of(1L);

        // ShippingAddress record 생성자 사용
        shippingAddress = new ShippingAddress(
                "홍길동",
                "010-1234-5678",
                "12345",
                "서울특별시 강남구 테헤란로 123",
                "456호",
                "부재시 경비실에 맡겨주세요"
        );

        orderItems = Arrays.asList(
                OrderItem.create(
                        OrderItemId.of(idGenerator.generateId()),
                        ProductId.of(1L),
                        "노트북",
                        Money.of(new BigDecimal("1500000")),
                        Quantity.of(2),
                        ProductOption.of(101L, "색상: 실버"),
                        Money.of(new BigDecimal("50000")) // 단위당 할인액
                ),
                OrderItem.create(
                        OrderItemId.of(idGenerator.generateId()),
                        ProductId.of(2L),
                        "마우스",
                        Money.of(new BigDecimal("50000")),
                        Quantity.of(3),
                        ProductOption.of(201L, "색상: 블랙"),
                        Money.of(new BigDecimal("5000")) // 단위당 할인액
                )
        );
    }

    @Test
    @DisplayName("새로운 주문을 생성할 수 있다")
    void createNewOrder() {
        // when
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);

        // then
        assertThat(order).isNotNull();
        assertThat(order.getMemberId()).isEqualTo(memberId);
        assertThat(order.getShippingAddress()).isEqualTo(shippingAddress);
        assertThat(order.getOrderItems()).hasSize(2);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        
        // 총액 계산: 
        // 노트북: (1,500,000 - 50,000) * 2 = 2,900,000
        // 마우스: (50,000 - 5,000) * 3 = 135,000
        // 총합: 2,900,000 + 135,000 = 3,035,000
        assertThat(order.getTotalAmount().getAmount())
                .isEqualByComparingTo(new BigDecimal("3035000"));
        assertThat(order.getDomainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("주문 항목이 없으면 주문을 생성할 수 없다")
    void cannotCreateOrderWithoutItems() {
        // when & then
        assertThatThrownBy(() -> Order.createNew(memberId, shippingAddress, List.of(), idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must have at least one item");
    }

    @Test
    @DisplayName("결제 정보를 추가하고 주문을 확정할 수 있다")
    void confirmOrderWithPayment() {
        // given
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);

        // when
        order.confirmWithPayment(
                PaymentMethodType.CREDIT_CARD,
                order.getTotalAmount(),
                idGenerator
        );

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPayments()).hasSize(1);
        assertThat(order.getPayments().get(0).getPaymentMethod()).isEqualTo(PaymentMethodType.CREDIT_CARD);
        assertThat(order.getDomainEvents()).hasSize(2); // OrderCreated + OrderPaid
    }

    @Test
    @DisplayName("대기 중이 아닌 주문은 확정할 수 없다")
    void cannotConfirmNonPendingOrder() {
        // given
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);
        order.confirmWithPayment(
                PaymentMethodType.CREDIT_CARD,
                order.getTotalAmount(),
                idGenerator
        );

        // when & then
        assertThatThrownBy(() -> order.confirmWithPayment(
                PaymentMethodType.CREDIT_CARD,
                order.getTotalAmount(),
                idGenerator
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order cannot be confirmed in status: PAID");
    }

    @Test
    @DisplayName("주문을 취소할 수 있다")
    void cancelOrder() {
        // given
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);
        String cancelReason = "고객 요청";

        // when
        order.cancel(cancelReason);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelReason()).isEqualTo(cancelReason);
        assertThat(order.getOrderItems())
                .allMatch(item -> item.getStatus() == OrderItemStatus.CANCELLED);
        assertThat(order.getDomainEvents()).hasSize(2); // OrderCreated + OrderCancelled
    }

    @Test
    @DisplayName("배송 중인 주문은 취소할 수 없다")
    void cannotCancelShippingOrder() {
        // given
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);
        order.confirmWithPayment(
                PaymentMethodType.CREDIT_CARD,
                order.getTotalAmount(),
                idGenerator
        );
        order.startShipping("TRACK-123456");

        // when & then
        assertThatThrownBy(() -> order.cancel("고객 요청"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order cannot be cancelled in status: SHIPPING");
    }

    @Test
    @DisplayName("배송을 시작할 수 있다")
    void startShipping() {
        // given
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);
        order.confirmWithPayment(
                PaymentMethodType.CREDIT_CARD,
                order.getTotalAmount(),
                idGenerator
        );

        // when
        order.startShipping("TRACK-123456");

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPING);
        assertThat(order.getOrderItems())
                .allMatch(item -> item.getStatus() == OrderItemStatus.SHIPPING);
    }

    @Test
    @DisplayName("배송을 완료할 수 있다")
    void completeDelivery() {
        // given
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);
        order.confirmWithPayment(
                PaymentMethodType.CREDIT_CARD,
                order.getTotalAmount(),
                idGenerator
        );
        order.startShipping("TRACK-123456");

        // when
        order.completeDelivery();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getOrderItems())
                .allMatch(item -> item.getStatus() == OrderItemStatus.DELIVERED);
    }

    @Test
    @DisplayName("주문을 완료(구매 확정)할 수 있다")
    void completeOrder() {
        // given
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);
        order.confirmWithPayment(
                PaymentMethodType.CREDIT_CARD,
                order.getTotalAmount(),
                idGenerator
        );
        order.startShipping("TRACK-123456");
        order.completeDelivery();

        // when
        order.complete();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("기존 주문을 복원할 수 있다")
    void restoreOrder() {
        // given
        OrderId orderId = OrderId.of(1234567890L);
        OrderNumber orderNumber = OrderNumber.of("ORD-20240101120000-1234");
        LocalDateTime orderedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime modifiedAt = LocalDateTime.now().minusHours(1);

        OrderPayment payment = OrderPayment.create(
                orderId.getValue(),
                PaymentMethodType.CREDIT_CARD,
                Money.of(new BigDecimal("3035000")),
                idGenerator
        );

        // when
        Order restoredOrder = Order.restore(
                orderId,
                orderNumber,
                memberId,
                shippingAddress,
                orderItems,
                Money.of(new BigDecimal("3035000")),
                OrderStatus.PAID,
                orderedAt,
                modifiedAt,
                List.of(payment),
                null
        );

        // then
        assertThat(restoredOrder).isNotNull();
        assertThat(restoredOrder.getId()).isEqualTo(orderId);
        assertThat(restoredOrder.getOrderNumber()).isEqualTo(orderNumber);
        assertThat(restoredOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(restoredOrder.getPayments()).hasSize(1);
        assertThat(restoredOrder.getOrderedAt()).isEqualTo(orderedAt);
        assertThat(restoredOrder.getModifiedAt()).isEqualTo(modifiedAt);
        assertThat(restoredOrder.getDomainEvents()).isEmpty(); // 복원 시에는 이벤트 발생 없음
    }

    @Test
    @DisplayName("할인 전 원래 금액을 계산할 수 있다")
    void calculateOriginalAmount() {
        // given
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);

        // when
        Money originalAmount = order.getOriginalAmount();

        // then
        // 노트북: 1,500,000 * 2 = 3,000,000
        // 마우스: 50,000 * 3 = 150,000
        // 총합: 3,000,000 + 150,000 = 3,150,000
        assertThat(originalAmount.getAmount())
                .isEqualByComparingTo(new BigDecimal("3150000"));
    }

    @Test
    @DisplayName("총 할인 금액을 계산할 수 있다")
    void calculateDiscountAmount() {
        // given
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);

        // when
        Money discountAmount = order.getDiscountAmount();

        // then
        // 노트북: 50,000 * 2 = 100,000
        // 마우스: 5,000 * 3 = 15,000
        // 총합: 100,000 + 15,000 = 115,000
        assertThat(discountAmount.getAmount())
                .isEqualByComparingTo(new BigDecimal("115000"));
    }

    @Test
    @DisplayName("최종 결제 금액을 반환할 수 있다")
    void getPayAmount() {
        // given
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);

        // when
        Money payAmount = order.getPayAmount();

        // then
        // payAmount는 totalAmount와 동일 (할인 적용 후 최종 금액)
        assertThat(payAmount).isEqualTo(order.getTotalAmount());
        assertThat(payAmount.getAmount())
                .isEqualByComparingTo(new BigDecimal("3035000"));
    }

    @Test
    @DisplayName("금액 계산이 올바른지 검증할 수 있다")
    void validateAmounts() {
        // given
        Order order = Order.createNew(memberId, shippingAddress, orderItems, idGenerator);

        // when
        boolean isValid = order.validateAmounts();

        // then
        assertThat(isValid).isTrue();
        
        // 검증: 원래금액 - 할인금액 = 결제금액
        // 3,150,000 - 115,000 = 3,035,000
        Money calculated = Money.of(
                order.getOriginalAmount().getAmount()
                        .subtract(order.getDiscountAmount().getAmount())
        );
        assertThat(calculated).isEqualTo(order.getPayAmount());
    }

    /**
     * 테스트용 IdGenerator 구현
     */
    private static class TestIdGenerator implements IdGenerator {
        private long sequence = 1000000000000L;

        @Override
        public synchronized long generateId() {
            return sequence++;
        }
    }
}
