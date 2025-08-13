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
    private TokenType tokenType;
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
        token.tokenType = type;
        token.issuedAt = LocalDateTime.now();
        token.expiresAt = LocalDateTime.now().plusSeconds(expirySeconds);
        return token;
    }
}
