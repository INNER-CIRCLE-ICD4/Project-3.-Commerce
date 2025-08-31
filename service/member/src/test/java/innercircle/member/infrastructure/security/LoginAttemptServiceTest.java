package innercircle.member.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;
    private final String testIp = "192.168.1.100";

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    void 처음_로그인_실패시_차단되지_않음() {
        loginAttemptService.recordFailedAttempt(testIp);

        assertThat(loginAttemptService.isBlocked(testIp)).isFalse();
        assertThat(loginAttemptService.getCurrentAttemptCount(testIp)).isEqualTo(1);
    }

    @Test
    void 다섯_번_로그인_실패시_차단됨() {
        for (int i = 0; i < 4; i++) {
            loginAttemptService.recordFailedAttempt(testIp);
            assertThat(loginAttemptService.isBlocked(testIp)).isFalse();
        }

        loginAttemptService.recordFailedAttempt(testIp);

        assertThat(loginAttemptService.isBlocked(testIp)).isTrue();
        assertThat(loginAttemptService.getCurrentAttemptCount(testIp)).isEqualTo(5);
    }

    @Test
    void 로그인_성공시_카운트_초기화() {
        loginAttemptService.recordFailedAttempt(testIp);
        loginAttemptService.recordFailedAttempt(testIp);

        assertThat(loginAttemptService.getCurrentAttemptCount(testIp)).isEqualTo(2);

        loginAttemptService.recordSuccessfulLogin(testIp);

        assertThat(loginAttemptService.getCurrentAttemptCount(testIp)).isEqualTo(0);
        assertThat(loginAttemptService.isBlocked(testIp)).isFalse();
    }

    @Test
    void 다른_IP는_영향받지_않음() {
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.2";

        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailedAttempt(ip1);
        }

        assertThat(loginAttemptService.isBlocked(ip1)).isTrue();
        assertThat(loginAttemptService.isBlocked(ip2)).isFalse();
        assertThat(loginAttemptService.getCurrentAttemptCount(ip1)).isEqualTo(5);
        assertThat(loginAttemptService.getCurrentAttemptCount(ip2)).isEqualTo(0);

    }


}