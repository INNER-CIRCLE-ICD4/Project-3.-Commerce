package innercircle.member.domain.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtTokenInfo {
    private Long userId;
    private String email;
    private List<String> roles = new ArrayList<>();
    private TokenType type;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    public static JwtTokenInfo create(
            Long userId,
            String email,
            List<String> roles,
            TokenType type,
            int expirySeconds
    ) {

        //todo: 이메일 형식 검증 추가 필요
        JwtTokenInfo token = new JwtTokenInfo();
        token.userId = userId;
        token.email = email;
        token.roles = roles;
        token.type = type;
        token.issuedAt = LocalDateTime.now();
        token.expiresAt = LocalDateTime.now().plusSeconds(expirySeconds);
        return token;
    }


    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Access Token 여부 확인
     */
    public boolean isAccessToken() {
        return type == TokenType.ACCESS;
    }

    /**
     * Refresh Token 여부 확인
     */
    public boolean isRefreshToken() {
        return type == TokenType.REFRESH;
    }

    /**
     * 특정 역할 보유 여부 확인
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * 관리자 권한 여부 확인
     * todo: 역할 상수로 관리하는 것이 좋다.
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    @Override
    public String toString() {
        return "JwtTokenInfo{" +
                "memberId=" + userId +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", type=" + type +
                ", issuedAt=" + issuedAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
