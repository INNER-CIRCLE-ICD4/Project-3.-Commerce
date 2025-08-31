package commerce.gateway.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RateLimitResult {

    private boolean allowed;    // 요청 허용 여부
    private int remainingTokens; // 남은 토큰 수
    private int limitCapacity;     // 버킷 용량
    private long retryAfterSeconds; // 재시도 가능 시간 (초 단위)


    /**
     * 요청 차단 결과 생성
     */
    public static RateLimitResult blocked(int capacity, long retryAfter) {
        return new RateLimitResult(false, 0, capacity, retryAfter);
    }

    /**
     * 요청 허용 결과 생성
     */
    public static RateLimitResult allowed(int remaining, int capacity) {
        return new RateLimitResult(true, remaining, capacity, 0);
    }

    /**
     * 허용률 계산
     */
    public double getAllowedRatio() {
        if (limitCapacity == 0) return 0.0;
        return (double) remainingTokens / limitCapacity;
    }


}
