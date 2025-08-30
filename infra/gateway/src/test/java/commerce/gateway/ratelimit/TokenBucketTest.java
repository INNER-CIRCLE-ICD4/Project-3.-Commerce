package commerce.gateway.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketTest {

    @Test
    @DisplayName("TokenBucket 기본 생성 테스트")
    void testTokenBucketCreation() {
        int capacity = 10;
        int refillRate = 5;

        TokenBucket bucket = new TokenBucket(capacity, refillRate);

        assertThat(capacity).isEqualTo(bucket.getCapacity());
        assertThat(refillRate).isEqualTo(bucket.getRefillRate());
        assertThat(capacity).isEqualTo(bucket.getTokens().get());
    }

    @Test
    @DisplayName("TokenBucket 토큰 소비 테스트")
    void testTokenConsumption() {
        TokenBucket bucket = new TokenBucket(10, 5);

        assertThat(bucket.tryConsume(1)).isTrue();
        assertThat(bucket.tryConsume(5)).isTrue();
        assertThat(bucket.getTokens().get()).isEqualTo(4);
    }

    @Test
    @DisplayName("TokenBucket 토큰 부족 시 소비 실패 테스트")
    void testTokenConsumptionFailure() {
        TokenBucket bucket = new TokenBucket(5, 2);

        assertThat(bucket.tryConsume(5)).isTrue();
        assertThat(bucket.tryConsume(1)).isFalse();
        assertThat(bucket.getTokens().get()).isEqualTo(0);
    }

    @Test
    @DisplayName("TokenBucket 토큰 보충 테스트")
    void testTokenRefill() throws InterruptedException {
        TokenBucket bucket = new TokenBucket(10, 5);
        bucket.tryConsume(10);
        assertThat(0).isEqualTo(bucket.getTokens().get());


        Thread.sleep(1100);
        bucket.refill();

        assertThat(bucket.getTokens().get()).isGreaterThanOrEqualTo(5);
    }

}