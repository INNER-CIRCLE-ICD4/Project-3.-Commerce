package innercircle.commerce.order.domain.model.entity;

import innercircle.commerce.order.domain.model.vo.enums.ShippingStatus;
import innercircle.commerce.order.domain.services.IdGenerator;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shipping Entity
 * 배송 정보를 관리하는 엔티티
 */
@Getter
public class Shipping {
    
    private final Long id;
    private final Long orderItemId;
    private String carrier;
    private String trackingNumber;
    private ShippingStatus currentStatus;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private final LocalDateTime createdAt;
    private final List<ShippingStatusHistory> statusHistory;
    
    private Shipping(
            Long id,
            Long orderItemId,
            String carrier,
            String trackingNumber,
            ShippingStatus currentStatus,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt,
            LocalDateTime createdAt) {
        this.id = id;
        this.orderItemId = orderItemId;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.currentStatus = currentStatus;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
        this.createdAt = createdAt;
        this.statusHistory = new ArrayList<>();
    }
    
    /**
     * 새로운 배송 생성
     */
    public static Shipping create(
            Long orderItemId,
            String carrier,
            String trackingNumber,
            IdGenerator idGenerator) {
        
        Shipping shipping = new Shipping(
                idGenerator.generateId(),
                orderItemId,
                carrier,
                trackingNumber,
                ShippingStatus.READY,
                null,
                null,
                LocalDateTime.now()
        );
        
        shipping.addStatusHistory(ShippingStatus.READY, "배송 준비", idGenerator);
        return shipping;
    }
    
    /**
     * 기존 배송 복원
     */
    public static Shipping restore(
            Long id,
            Long orderItemId,
            String carrier,
            String trackingNumber,
            ShippingStatus currentStatus,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt,
            LocalDateTime createdAt) {
        
        return new Shipping(
                id,
                orderItemId,
                carrier,
                trackingNumber,
                currentStatus,
                shippedAt,
                deliveredAt,
                createdAt
        );
    }
    
    /**
     * 배송 시작
     */
    public void startShipping(IdGenerator idGenerator) {
        // TODO: 배송 시작 로직 구현
        validateCanStart();

        this.currentStatus = ShippingStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
        addStatusHistory(ShippingStatus.SHIPPED, "배송 시작", idGenerator);
    }
    
    /**
     * 배송 중 상태 업데이트
     */
    public void updateStatus(ShippingStatus newStatus, String note, IdGenerator idGenerator) {
        // TODO: 배송 상태 업데이트 로직 구현
        validateStatusTransition(newStatus);
        
        this.currentStatus = newStatus;
        addStatusHistory(newStatus, note, idGenerator);
    }
    
    /**
     * 배송 완료
     */
    public void completeDelivery(IdGenerator idGenerator) {
        // TODO: 배송 완료 로직 구현
        validateCanComplete();
        
        this.currentStatus = ShippingStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        addStatusHistory(ShippingStatus.DELIVERED, "배송 완료", idGenerator);
    }
    
    /**
     * 상태 이력 추가
     */
    private void addStatusHistory(ShippingStatus status, String note, IdGenerator idGenerator) {
        ShippingStatusHistory history = ShippingStatusHistory.create(
                this.id,
                status,
                note,
                idGenerator
        );
        this.statusHistory.add(history);
    }
    
    /**
     * 상태 이력 조회 (읽기 전용)
     */
    public List<ShippingStatusHistory> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }
    
    private void validateCanStart() {
        if (this.currentStatus != ShippingStatus.READY) {
            throw new IllegalStateException("Shipping can only be started from READY status");
        }
    }
    
    private void validateCanComplete() {
        if (this.currentStatus != ShippingStatus.IN_TRANSIT) {
            throw new IllegalStateException("Shipping can only be completed from IN_TRANSIT status");
        }
    }
    
    private void validateStatusTransition(ShippingStatus newStatus) {
        if (!this.currentStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", this.currentStatus, newStatus)
            );
        }
    }
}
