package innercircle.member.infrastructure.adapter.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenAdapterTest {

    private JwtTokenAdapter jwtTokenAdapter;

    @BeforeEach
    void setUp() {
        // Any setup code can go here if needed
        String safeSecret = "mySecretKey123456789012345678901234567890abcdefghijklmnopqrstuvwxyz12345";
        jwtTokenAdapter = new JwtTokenAdapter(
                safeSecret,           // 64자 = 256비트 이상
                3600000L,            // 1시간
                604800000L           // 7일
        );
    }


    @Test
    void generateAccessToken_shouldCreateValidToken() {


        String token = jwtTokenAdapter.generateAccessToken(1L, "sw.noh@google.com", List.of("BUYER", "SELLER"));

        assertThat(jwtTokenAdapter.validateToken(token)).isTrue();
        assertThat(jwtTokenAdapter.getUserIdFromToken(token)).isEqualTo(1L);
        assertThat(jwtTokenAdapter.getEmailFromToken(token)).isEqualTo("sw.noh@google.com");
        assertThat(jwtTokenAdapter.getRolesFromToken(token)).containsExactly("BUYER", "SELLER");
    }


    @Test
    void generateRefreshToken_shouldCreateValidToken() {

        String refreshToken = jwtTokenAdapter.generateRefreshToken(1L, "sw.noh@google.com", List.of("BUYER"));

        assertThat(jwtTokenAdapter.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenAdapter.getUserIdFromToken(refreshToken)).isEqualTo(1L);
        assertThat(jwtTokenAdapter.getEmailFromToken(refreshToken)).isEqualTo("sw.noh@google.com");
        assertThat(jwtTokenAdapter.getRolesFromToken(refreshToken)).containsExactly("BUYER");
    }

    @Test
    void validationToken_shouldReturnFalseForInvalidToken() {
        String invalid = "invalidToken123456789012345678901234567890abcdefghijklmnopqrstuvwxyz12345";
        assertThat(jwtTokenAdapter.validateToken(invalid)).isFalse();
    }

    @Test
    void validationToken_shouldReturnFalseForExpiredToken() {
        JwtTokenAdapter  shortExpiryAdapter = new JwtTokenAdapter(
                "mySecretKey123456789012345678901234567890abcdefghijklmnopqrstuvwxyz12345",
                1L,  // 1ms로 즉시 만료
                1L
        );

        String token = shortExpiryAdapter.generateAccessToken(1L, "test@gmail.com", List.of("BUYER"));
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThat(shortExpiryAdapter.validateToken(token)).isFalse();
    }
}