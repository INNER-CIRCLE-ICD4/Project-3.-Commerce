package innercircle.commerce.order.domain.model.entity;

import innercircle.commerce.order.domain.model.vo.enums.ShippingStatus;
import innercircle.commerce.order.domain.services.IdGenerator;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * ShippingStatusHistory
 * 배송 상태 이력을 관리
 */
@Getter
public class ShippingStatusHistory {
    
    private final Long id;
    private final Long shippingId;
    private final ShippingStatus status;
    private final String note;
    private final LocalDateTime createdAt;
    
    private ShippingStatusHistory(
            Long id,
            Long shippingId,
            ShippingStatus status,
            String note,
            LocalDateTime createdAt) {
        this.id = id;
        this.shippingId = shippingId;
        this.status = status;
        this.note = note;
        this.createdAt = createdAt;
    }
    
    /**
     * 새로운 배송 상태 이력 생성
     */
    public static ShippingStatusHistory create(
            Long shippingId,
            ShippingStatus status,
            String note,
            IdGenerator idGenerator
    ) {
        return new ShippingStatusHistory(
                idGenerator.generateId(),
                shippingId,
                status,
                note,
                LocalDateTime.now()
        );
    }
    
    /**
     * 기존 배송 상태 이력 복원
     */
    public static ShippingStatusHistory restore(
            Long id,
            Long shippingId,
            ShippingStatus status,
            String note,
            LocalDateTime createdAt) {
        
        return new ShippingStatusHistory(
                id,
                shippingId,
                status,
                note,
                createdAt
        );
    }
}
