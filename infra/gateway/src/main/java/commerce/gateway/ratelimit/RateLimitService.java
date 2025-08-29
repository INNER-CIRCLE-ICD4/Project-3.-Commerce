package commerce.gateway.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class RateLimitService {
    private final Map<String, TokenBucket> tokenBuckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicInteger totalBlocked = new AtomicInteger(0);
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private volatile LocalDateTime lastCleanupTime = LocalDateTime.now();


    public RateLimitService() {

        // 5초마다 모든 버킷 보충
        scheduler.scheduleAtFixedRate(this::refillAllBuckets, 5, 5, TimeUnit.SECONDS);

        // 30분마다 오래된 버킷 정리
        scheduler.scheduleAtFixedRate(this::cleanupOldBuckets, 30, 30, TimeUnit.MINUTES);

        log.info("RateLimitService initialized - 토큰 보충 : 5초마다, 오래된 버킷 정리: 30분마다");
    }

    /**
     * Rate Limit 체크 (핵심 메서드)
     */
    public RateLimitResult checkLimit(String key, RateLimitConfig config) {
        totalRequests.incrementAndGet();

        TokenBucket tokenBucket = tokenBuckets.computeIfAbsent(key,
                k -> new TokenBucket(config.getCapacity(), config.getRefillRate()));

        boolean allowed = tokenBucket.tryConsume(config.getRequestTokens());


        if (allowed) {
            // 허용된 요청
            int remaining = tokenBucket.getTokens().get();
            log.debug("요청 허용: key={}, remaining={}/{}", key, remaining, config.getRequestTokens());
            return RateLimitResult.allowed(remaining, config.getCapacity());
        } else {
            totalBlocked.incrementAndGet();
            long retryAfter = tokenBucket.getSecondUnitRefill();
            log.warn("요청 차단: key={}, config={}, 용량 초과 (남은 토큰: 0), 재시도 가능 시간: {}초", key, config.getCapacity(), retryAfter);
            return RateLimitResult.blocked(config.getCapacity(), retryAfter);
        }
    }


    /**
     * 모든 버킷 보충
     */
    private void refillAllBuckets() {
        if (tokenBuckets.isEmpty()) {
            return;
        }

        tokenBuckets.values().parallelStream().forEach(TokenBucket::refill);

        log.debug("모든 토큰 버킷 보충 완료: 버킷 수={}, 총 요청 수={}", tokenBuckets.size(), totalRequests.get());
    }

    /**
     * 오래된 버킷 정리 (1 시간 이상 사용되지 않은 것)
     */
    private void cleanupOldBuckets() {
        long cutoffTime = System.currentTimeMillis() - (60 * 60 * 1000); // 1시간
        AtomicInteger removed = new AtomicInteger(0);

        tokenBuckets.entrySet().removeIf(entry -> {
            TokenBucket bucket = entry.getValue();
            if (bucket.getLastRefillTime() < cutoffTime) {
                removed.incrementAndGet();
                return true;
            }
            return false;
        });
    }


    /**
     * 📊 현재 상태 조회 (모니터링용)
     */
    public Map<String, Object> getStats() {
        int currentBuckets = tokenBuckets.size();
        int blocked = totalBlocked.get();
        int requests = totalRequests.get();
        double blockRate = requests > 0 ? (double) blocked / requests * 100 : 0.0;

        return Map.of(
                "totalBuckets", currentBuckets,
                "totalRequests", requests,
                "totalBlocked", blocked,
                "blockRate", String.format("%.2f%%", blockRate),
                "lastCleanupTime", lastCleanupTime.toString(),
                "bucketDetails", getBucketDetails()
        );
    }

    /**
     * 🔍 버킷 상세 정보 (상위 10개만 - 메모리 절약)
     */
    private Map<String, Object> getBucketDetails() {
        return tokenBuckets.entrySet().stream()
                .limit(10) // 너무 많으면 응답이 커지므로 제한
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            TokenBucket bucket = entry.getValue();
                            return Map.of(
                                    "tokens", bucket.getTokens().get(),
                                    "capacity", bucket.getCapacity(),
                                    "refillRate", bucket.getRefillRate(),
                                    "lastRefillTime", bucket.getLastRefillTime()
                            );
                        }
                ));
    }

    /**
     * 🔄 특정 키의 Rate Limit 리셋
     */
    public void resetLimit(String key) {
        TokenBucket removed = tokenBuckets.remove(key);
        if (removed != null) {
            log.info("🔄 Rate limit 리셋: key={}", key);
        } else {
            log.warn("⚠️ Rate limit 리셋 실패 - 키를 찾을 수 없음: key={}", key);
        }
    }

    /**
     * 🔄 모든 Rate Limit 리셋
     */
    public void resetAllLimits() {
        int count = tokenBuckets.size();
        tokenBuckets.clear();
        totalBlocked.set(0);
        totalRequests.set(0);
        log.info("🔄 모든 Rate limit 리셋: {} 개 버킷 삭제", count);
    }

    /**
     * 📈 현재 활성 버킷 수
     */
    public int getActiveBucketCount() {
        return tokenBuckets.size();
    }

    /**
     * 📊 차단 비율 계산
     */
    public double getBlockRate() {
        int requests = totalRequests.get();
        int blocked = totalBlocked.get();
        return requests > 0 ? (double) blocked / requests * 100 : 0.0;
    }


}
