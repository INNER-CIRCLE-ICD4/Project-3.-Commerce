package innercircle.commerce.order.domain.model.entity;

import innercircle.commerce.order.domain.model.vo.enums.OrderItemStatus;
import innercircle.commerce.order.domain.services.IdGenerator;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * OrderStatusHistory Entity
 * 주문 상태 이력을 관리하는 엔티티
 */
@Getter
public class OrderStatusHistory {

    private final Long id;
    private final Long orderItemId;
    private final OrderItemStatus status;
    private final String note;
    private final LocalDateTime changedAt;

    // Private Constructor
    private OrderStatusHistory(
            Long id,
            Long orderItemId,
            OrderItemStatus status,
            String note,
            LocalDateTime changedAt) {
        this.id = id;
        this.orderItemId = orderItemId;
        this.status = status;
        this.note = note;
        this.changedAt = changedAt;
    }

    /**
     * 새로운 주문 상태 이력 생성
     */
    public static OrderStatusHistory create(
            Long orderItemId,
            OrderItemStatus status,
            String note,
            IdGenerator idGenerator) {

        return new OrderStatusHistory(
                idGenerator.generateId(),
                orderItemId,
                status,
                note,
                LocalDateTime.now()
        );
    }

    /**
     * 기존 주문 상태 이력 복원
     */
    public static OrderStatusHistory restore(
            Long id,
            Long orderItemId,
            OrderItemStatus status,
            String note,
            LocalDateTime changedAt) {

        return new OrderStatusHistory(
                id,
                orderItemId,
                status,
                note,
                changedAt
        );
    }
}
