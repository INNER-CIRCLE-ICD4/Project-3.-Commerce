package innercircle.member.infrastructure.adapter.out;

import innercircle.member.application.port.out.TokenPort;
import innercircle.member.domain.auth.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenAdapter implements TokenPort {

    private final SecretKey secretKey;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtTokenAdapter(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }


    @Override
    public String generateAccessToken(Long userId, String email, List<String> roles) {
        return generateToken(userId, email, roles, TokenType.ACCESS, accessTokenExpiry);
    }

    @Override
    public String generateRefreshToken(Long userId, String email, List<String> roles) {
        return generateToken(userId, email, roles, TokenType.REFRESH, refreshTokenExpiry);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (SecurityException e) {
            log.error("잘못된 JWT 서명입니다.", e);
        } catch (MalformedJwtException e) {
            log.error("잘못된 JWT 토큰입니다.", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.", e);
        }

        return false;
    }

    @Override
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.getSubject());
    }

    @Override
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("email", String.class);
    }

    @Override
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String roles = claims.get("roles", String.class);
        return List.of(roles.split(","));
    }


    private String generateToken(Long userId, String email, List<String> roles, TokenType tokenType, long expiry) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiry);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("roles", String.join(",", roles))
                .claim("type", tokenType.name())
                .issuedAt(now)
                .id(jti)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();

    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
