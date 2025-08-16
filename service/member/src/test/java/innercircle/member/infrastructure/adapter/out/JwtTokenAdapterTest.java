package innercircle.member.infrastructure.adapter.out;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

//@ExtendWith(MockitoExtension.class)
class JwtTokenAdapterTest {


    @Test
    void generateToken_shouldCreateValidToken() {


        JwtTokenAdapter adapter = new JwtTokenAdapter(
                "mySecretKey123456789012345678901234567890abcdefghijklmnopqrstuvwxyz12345",
                3600000L,
                604800000L
        );

        String token = adapter.generateAccessToken(1L, "sw.noh@google.com", List.of("BUYER"));

        assertThat(adapter.validateToken(token)).isTrue();
        assertThat(adapter.getUserIdFromToken(token)).isEqualTo(1L);
        assertThat(adapter.getEmailFromToken(token)).isEqualTo("sw.noh@google.com");

    }



}