package innercircle.commerce.order.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DomainEvent
 * 도메인 이벤트 베이스 클래스
 */
@Getter
public abstract class DomainEvent {
    
    private final String eventId;
    private final LocalDateTime occurredAt;
    
    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
    }
    
    /**
     * 이벤트가 발생한 Aggregate의 ID
     */
    public abstract String getAggregateId();
    
    /**
     * 이벤트 타입 (클래스명 또는 커스텀 이름)
     */
    public abstract String getEventType();
}
