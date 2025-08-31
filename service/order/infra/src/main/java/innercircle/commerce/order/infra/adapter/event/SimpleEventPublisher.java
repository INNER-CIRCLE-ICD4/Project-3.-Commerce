package innercircle.commerce.order.infra.adapter.event;

import innercircle.commerce.order.application.port.out.EventPublisher;
import innercircle.commerce.order.domain.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SimpleEventPublisher
 * 임시 이벤트 발행자 (로그만 출력)
 */
@Slf4j
@Component
public class SimpleEventPublisher implements EventPublisher {

    @Override
    public void publish(DomainEvent event) {
        log.info("Domain Event Published: {} | AggregateId: {} | EventId: {} | EventType: {}",
                event.getClass().getSimpleName(),
                event.getAggregateId(), 
                event.getEventId(),
                event.getEventType());
        
        log.debug("Event Details: {}", event);
        
        // Kafka 연동 시 KafkaEventPublisher로 교체
        // 현재는 로그만 출력하고 실제 발행은 하지 않음
    }
}
