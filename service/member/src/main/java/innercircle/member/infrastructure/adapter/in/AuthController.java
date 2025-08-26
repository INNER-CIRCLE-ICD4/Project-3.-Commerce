package innercircle.member.infrastructure.adapter.in;

import innercircle.member.application.port.in.AuthUseCase;
import innercircle.member.domain.auth.AuthToken;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginRequest;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginResponse;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.RefreshRequest;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.RefreshResponse;
import innercircle.member.infrastructure.adapter.in.web.auth.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthMapper authMapper;
    private final AuthUseCase authUseCase;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("인증 서버가 정상 작동 중입니다.");
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        LoginResponse loginResponse = authMapper.authTokenToLoginResponse(authUseCase.login(request));

        return getHeader().body(loginResponse);
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
}
