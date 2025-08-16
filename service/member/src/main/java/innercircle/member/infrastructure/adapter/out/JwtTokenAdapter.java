package innercircle.member.infrastructure.adapter.out;

import innercircle.member.application.port.out.TokenPort;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class JwtTokenAdapter implements TokenPort {

    private final SecretKey secretKey;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtTokenAdapter(
            @Value("${jwt.secret}") String secretKey,
            @Value("jwt.access-token-expiry") long accessTokenExpiry,
            @Value("jwt.refresh-token-expiry") long refreshTokenExpiry) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }


    @Override
    public String generateToken(Long userId, String email, List<String> roles) {
        return "";
    }

    @Override
    public String generateRefreshToken(Long userId, String email, List<String> roles) {
        return "";
    }

    @Override
    public boolean validateToken(String token) {
        return false;
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return 0L;
    }

    @Override
    public String getEmailFromToken(String token) {
        return "";
    }

    @Override
    public List<String> getRolesFromToken(String token) {
        return List.of();
    }
}
