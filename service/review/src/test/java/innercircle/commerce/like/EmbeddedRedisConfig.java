package innercircle.commerce.like;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

@TestConfiguration
public class EmbeddedRedisConfig {
    private final RedisServer redisServer;

    public EmbeddedRedisConfig() {
        // 테스트용으로 사용할 Redis 서버의 포트를 지정 (기본 6379와 겹치지 않게)
        this.redisServer = new RedisServer(16379);
    }

    @PostConstruct // 테스트 컨텍스트가 생성될 때 실행
    public void startRedis() {
        redisServer.start();
    }

    @PreDestroy // 테스트 컨텍스트가 소멸될 때 실행
    public void stopRedis() {
        redisServer.stop();
    }
}
