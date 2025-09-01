package innercircle.commerce.order.application.port.out;

import innercircle.commerce.order.domain.event.DomainEvent;

/**
 * EventPublisher Interface
 * 도메인 이벤트 발행을 위한 출력 포트
 */
public interface EventPublisher {
    /**
     * 도메인 이벤트를 발행합니다.
     *
     * @param event 발행할 도메인 이벤트
     */
    void publish(DomainEvent event);
}
