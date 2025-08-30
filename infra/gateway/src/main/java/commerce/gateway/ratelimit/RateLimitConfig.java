package commerce.gateway.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * Rate Limiting 설정 정보를 담는 클래스
 * API 별로 사전 정의된 설정을 제공한다.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitConfig {

    private int refillRate; // 초당 토큰 보충 속도
    private int capacity;   // 버킷 최대 용량
    private int requestTokens; // 요청당 소비되는 토큰 수

    /**
     * 로그인 API용
     * 브루트포스 공격 방어 목적
     */
    public static RateLimitConfig forAuth() {
        return new RateLimitConfig(2, 10, 1);
    }

    /**
     * 일반 API용
     * 서비스 전체 보호 목적(일반 사용자 편의 목적)
     */
     public static RateLimitConfig forGeneral() {
         return new RateLimitConfig(50, 100, 1);
     }

    /**
     * 관리자 API용 (중간 제한)
     * 중요한 관리 기능 보호
     */
    public static RateLimitConfig forAdmin() {
        return new RateLimitConfig(10, 30, 1);
    }


    /**
     * Health Check API용 (느슨한 제한)
     * 모니터링 빈번한 요청
     */
    public static RateLimitConfig forHealth() {
        return new RateLimitConfig(100, 200, 1);
    }
}
