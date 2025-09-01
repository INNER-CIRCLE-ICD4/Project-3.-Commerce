package innercircle.commerce.order.domain.services;

/**
 * IdGenerator Interface
 * 도메인 계층의 ID 생성 인터페이스
 */
public interface IdGenerator {
    /**
     * 고유 ID 생성
     * @return Snowflake 기반 고유 ID (Long)
     */
    long generateId();
}
