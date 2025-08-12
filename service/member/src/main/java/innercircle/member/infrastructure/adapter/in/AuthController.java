package innercircle.member.infrastructure.adapter.in;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("인증 서버가 정상 작동 중입니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login() {

        return ResponseEntity.ok("로그인 성공");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh() {
        return ResponseEntity.ok("리프레시 토큰 발급 성공");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.noContent().build();
    }
}
