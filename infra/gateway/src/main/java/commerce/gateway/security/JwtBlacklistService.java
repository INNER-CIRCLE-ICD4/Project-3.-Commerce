package commerce.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class JwtBlacklistService {

    private final ConcurrentHashMap<String, LocalDateTime> blacklistTokens = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public JwtBlacklistService() {
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.MINUTES);
        log.info("JWT Blacklist Service 초기화 완료");
    }

    /**
     * 토큰을 블랙리스트에 추가한다.
     * @param jti
     * @param expirationDate
     */
    public void blacklistToken(String jti, Date expirationDate) {
        if (jti == null || jti.trim().isEmpty()) {
            log.warn("JTI가 Null이거나 비어있습니다. 토큰을 블랙리스트에 추가할 수 없습니다.");
            return;
        }

        LocalDateTime expiryTime = expirationDate.toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        blacklistTokens.put(jti, expiryTime);

        log.info("토큰이 블랙리스트에 추가되었습니다. JTI: {}, 만료 시간: {}", jti, expiryTime);
        log.debug("현재 블랙리스트 토큰 수: {}", blacklistTokens.size());
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인한다.
     */
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.trim().isEmpty()) {
            log.warn("JTI가 Null이거나 비어있습니다. 블랙리스트 확인을 건너뜁니다.");
            return false;
        }

        LocalDateTime expiry = blacklistTokens.get(jti);
        if (expiry == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(expiry)) {
            blacklistTokens.remove(jti);
            log.info("만료된 블랙리스트 토큰이 제거되었습니다. JTI: {}", jti);
            return false;
        }

        log.warn("블랙리스트에 있는 토큰입니다. JTI: {}", jti);
        return true;
    }



    private void cleanupExpiredTokens() {

        LocalDateTime now = LocalDateTime.now();
        AtomicInteger removedCount = new AtomicInteger(0);  // ✅ AtomicInteger 사용
        blacklistTokens.entrySet().removeIf(entry -> {
            if (now.isAfter(entry.getValue())) {
                removedCount.incrementAndGet();
                return true;
            }
            return false;
        });

        if (removedCount.get() > 0) {
            log.info("만료된 블랙리스트 토큰 {}개 제거 완료", blacklistTokens.size());
        }

    }

    // ✅ 애플리케이션 종료 시 스케줄러 정리
    public void shutdown() {
        scheduler.shutdown();
        log.info("🚪 JWT Blacklist Service 종료");
    }

}
