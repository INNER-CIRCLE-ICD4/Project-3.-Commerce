package commerce.gateway.ratelimit;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Getter
public class TokenBucket {

    private final int capacity;
    private final int refillRate;
    private final AtomicInteger tokens;
    private volatile long lastRefillTime;

    public TokenBucket(int capacity, int refillRate) {

        if (capacity <= 0 || refillRate <= 0) {
            throw new IllegalArgumentException("capacity and refillRate must be positive");
        }

        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = new AtomicInteger(capacity);
        this.lastRefillTime = System.currentTimeMillis();

        log.debug("TokenBucket initialized: capacity={}, refillRate={}", capacity, refillRate);
    }


    /**
     * 시간 기반 토큰 보충
     */
    public synchronized void refill() {
        long now = System.currentTimeMillis();
        long timePassed = now - lastRefillTime;

        if (timePassed >= 1000) { // 최소 1초 이상 경과했을 때만 보충
            long secondsPassed = timePassed / 1000;
            long tokensToAdd = Math.min(secondsPassed * refillRate, capacity);

            int currentTokens = tokens.get();
            int newTokens = Math.min(currentTokens + (int)tokensToAdd, capacity);

            if (newTokens > currentTokens) {
                tokens.set(newTokens);
                lastRefillTime = now;

                log.debug("토큰 보충: {} -> {} (추가된 토큰: {})", currentTokens, newTokens, tokensToAdd);
            }
        }
    }

    /**
     * 토큰 소비 시도
     */
    public boolean tryConsume(int requestTokens) {
        if(requestTokens <= 0) {
            throw new IllegalArgumentException("requestTokens must be positive");
        }

        refill();

        while (true) {
            int currentTokens = tokens.get();
            if (currentTokens < requestTokens) {
                log.debug("토큰 부족: 요청된 토큰 = {}, 보유 토큰 = {}", requestTokens, currentTokens);
                return false;
            }

            int newTokens = currentTokens - requestTokens;
            if (tokens.compareAndSet(currentTokens, newTokens)) {
                log.debug("토큰 소비 성공: 요청된 토큰 = {}, 남은 토큰 = {}", requestTokens, newTokens);
                return true;
            }
        }
    }

    /**
     * 다음 토큰 보충까지 대기 시간 계산
     */
    public long getSecondUnitRefill() {
        if (tokens.get() >= capacity) {
            return 0;
        }
        return Math.max(1, refillRate > 0 ? 1 : 60);
    }


    @Override
    public String toString() {
        return String.format("TokenBucket{capacity=%d, refillRate=%d, tokens=%d}", capacity, refillRate, tokens.get());
    }
}
