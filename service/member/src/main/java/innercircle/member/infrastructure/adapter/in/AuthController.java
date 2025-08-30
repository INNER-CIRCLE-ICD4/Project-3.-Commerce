package innercircle.member.infrastructure.adapter.in;

import innercircle.member.application.port.in.AuthUseCase;
import innercircle.member.domain.auth.AuthToken;
import innercircle.member.domain.auth.LoginFailedException;
import innercircle.member.domain.auth.TooManyAttemptsException;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginRequest;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginResponse;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.RefreshRequest;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.RefreshResponse;
import innercircle.member.infrastructure.adapter.in.web.auth.mapper.AuthMapper;
import innercircle.member.infrastructure.security.LoginAttemptService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginAttemptService loginAttemptService;
    private final AuthMapper authMapper;
    private final AuthUseCase authUseCase;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("인증 서버가 정상 작동 중입니다.");
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {

        String clientIp = getClientIp(httpServletRequest);
        loginAttemptService.validateIpNotBlocked(clientIp);

        try {

            AuthToken login = authUseCase.login(request);
            LoginResponse loginResponse = authMapper.authTokenToLoginResponse(login);
            loginAttemptService.recordSuccessfulLogin(clientIp);

            log.info("로그인 성공: email={}, ip={}", request.email(), clientIp);
            return getHeader().body(loginResponse);

        } catch (LoginFailedException e) {
            loginAttemptService.recordFailedAttempt(clientIp);
            log.warn("로그인 실패: email={}, ip={}, reason={}, 남은 횟수={}/{}", request.email(), clientIp, e.getMessage(), loginAttemptService.getCurrentAttemptCount(clientIp),loginAttemptService.getMaxAttempts());
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {

        RefreshResponse refreshResponse = authMapper.authTokenToRefreshResponse(authUseCase.refresh(request));

        return getHeader().body(refreshResponse);
    }


    private static ResponseEntity.BodyBuilder getHeader() {
        return ResponseEntity.ok()
                .header("X-Content-Type-Options", "nosniff")
                .header("X-Frame-Options", "DENY")
                .header("Cache-Control", "no-store, no-cache, must-revalidate");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
