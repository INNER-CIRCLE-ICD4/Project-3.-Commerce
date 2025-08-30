package commerce.gateway.ratelimit;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    @DisplayName("Rate Limit 허용테스트")
    void testRateLimitAllowed() {
        String key = "user-123";
        RateLimitConfig config = RateLimitConfig.forGeneral();

        RateLimitResult result = rateLimitService.checkLimit(key, config);

        assertThat(result.isAllowed()).isTrue();
        assertThat(99).isEqualTo(result.getRemainingTokens());
        assertThat(100).isEqualTo(result.getLimitCapacity());
    }

    @Test
    @DisplayName("Rate Limit 초과 테스트")
    void testRateLimitBlocked() {
        String key = "user-blocked";
        RateLimitConfig config = new RateLimitConfig(1, 2, 1);
        rateLimitService.checkLimit(key, config);
        rateLimitService.checkLimit(key, config);

        RateLimitResult result = rateLimitService.checkLimit(key, config);

        assertThat(result.isAllowed()).isFalse();
        assertThat(0).isEqualTo(result.getRemainingTokens());
        assertThat(result.getRetryAfterSeconds()).isGreaterThan(0);
    }

    @Test
    @DisplayName("다른 키는 독립적으로 제한")
    void testIndependentKeys() {
        String key1 = "user-1";
        String key2 = "user-2";

        RateLimitConfig config = new RateLimitConfig(1, 1, 1);

        RateLimitResult result1 = rateLimitService.checkLimit(key1, config);
        RateLimitResult result2 = rateLimitService.checkLimit(key2, config);

        assertThat(result1.isAllowed()).isTrue();
        assertThat(result2.isAllowed()).isTrue();
    }

    @Test
    @DisplayName("Rate Limit 리셋 테스트")
    void resetLimit() {
        String key = "user-reset";
        RateLimitConfig config = new RateLimitConfig(1, 1, 1);
        rateLimitService.checkLimit(key, config);

        rateLimitService.resetLimit(key);
        RateLimitResult result = rateLimitService.checkLimit(key, config);
        assertThat(result.isAllowed()).isTrue();
    }
}