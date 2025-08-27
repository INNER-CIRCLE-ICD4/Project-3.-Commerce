package innercircle.member.infrastructure.security;

import innercircle.global.auth.AuthErrorCode;
import innercircle.member.domain.auth.TooManyAttemptsException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component

public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 15;

    private final ConcurrentHashMap<String, AttemptInfo> attemptCache = new ConcurrentHashMap<>();

    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }
    /**
     * IP 차단 확인 및 예외 발생
     */
    public void validateIpNotBlocked(String clientIp) {
        AttemptInfo info = attemptCache.get(clientIp);
        if (info != null && info.isLocked() && !info.shouldUnlock()) {
            // ✅ 예외 발생으로 GlobalExceptionHandler에 위임
            throw new TooManyAttemptsException(AuthErrorCode.TOO_MANY_ATTEMPTS, clientIp, info.getAttemptCount(), LOCK_TIME_MINUTES ,"IP " + clientIp + "가 차단되었습니다.");
        }

        // 잠금 해제된 경우 정리
        if (info != null && info.shouldUnlock()) {
            attemptCache.remove(clientIp);
            log.info("🔓 IP 잠금 해제: ip={}", clientIp);
        }
    }

    /**
     * IP가 차단되었는지 확인
     */
    public boolean isBlocked(String clientIp) {
        AttemptInfo attemptInfo = attemptCache.get(clientIp);
        if(attemptInfo == null) {
            return false;
        }

        if (attemptInfo.isLocked() && attemptInfo.shouldUnlock()) {
            attemptCache.remove(clientIp);
            log.info("IP {}의 차단이 해제되었습니다.", clientIp);
            return false;
        }

        return attemptInfo.isLocked();
    }

    /**
     * 로그인 실패 기록
     */
    public void recordFailedAttempt(String clientIp) {
        attemptCache.compute(clientIp, (key, info) -> {
            if (info == null) {
                log.info("첫 번째 로그인 실패: ip ={}", clientIp);
                return new AttemptInfo(1, LocalDateTime.now());
            }
            AttemptInfo newInfo = info.incrementAttempt();

            if (newInfo.isLocked()) {
                log.warn("IP {}가 차단되었습니다. ({}분 동안)", clientIp, LOCK_TIME_MINUTES);
            } else {
                log.info("로그인 실패: ip ={}, 시도 횟수 ={}/{}", clientIp, newInfo.getAttemptCount(), MAX_ATTEMPTS);
            }

            return newInfo;
        });
    }

    /**
     * 로그인 성공 시 기록 초기화
     */
    public void recordSuccessfulLogin(String clientIp) {
        AttemptInfo removed = attemptCache.remove(clientIp);
        if (removed != null && removed.getAttemptCount() > 1) {
            log.info("로그인 성공: ip ={}, 이전 실패 횟수 ={}", clientIp, removed.getAttemptCount());
        }
    }

    /**
     * 현재 시도 횟수 조회(로깅용)
     */
    public int getCurrentAttemptCount(String clientIp) {
        AttemptInfo attemptInfo = attemptCache.get(clientIp);
        return attemptInfo != null ? attemptInfo.getAttemptCount() : 0;
    }

    // 내부 클래스
    private static class AttemptInfo {
        private final int attemptCount;
        private final LocalDateTime lastAttempt;

        public AttemptInfo(int attemptCount, LocalDateTime lastAttempt) {
            this.attemptCount = attemptCount;
            this.lastAttempt = lastAttempt;
        }

        public AttemptInfo incrementAttempt() {
            return new AttemptInfo(this.attemptCount + 1, LocalDateTime.now());
        }

        public boolean isLocked() {
            return attemptCount >= MAX_ATTEMPTS;
        }

        public boolean shouldUnlock() {
            return ChronoUnit.MINUTES.between(lastAttempt, LocalDateTime.now()) >= LOCK_TIME_MINUTES;
        }

        public int getAttemptCount() {
            return attemptCount;
        }
    }

}
