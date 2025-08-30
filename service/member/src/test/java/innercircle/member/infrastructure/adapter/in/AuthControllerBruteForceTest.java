package innercircle.member.infrastructure.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import innercircle.config.SecurityConfig;
import innercircle.global.auth.AuthErrorCode;
import innercircle.member.application.port.in.AuthUseCase;
import innercircle.member.application.port.out.TokenPort;
import innercircle.member.domain.auth.LoginFailedException;
import innercircle.member.domain.auth.TooManyAttemptsException;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginRequest;
import innercircle.member.infrastructure.adapter.in.web.auth.mapper.AuthMapper;
import innercircle.member.infrastructure.adapter.out.JwtTokenAdapter;
import innercircle.member.infrastructure.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.TooManyListenersException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class})
public class AuthControllerBruteForceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    LoginAttemptService loginAttemptService;
    @MockitoBean
    AuthUseCase authUseCase;
    @MockitoBean
    AuthMapper authMapper;

    private final String testIp = "192.168.1.100";

    @Test
    void 로그인_5번_실패_후_차단됨() throws Exception {

        LoginRequest request = new LoginRequest("test@gmail.com", "wrongpassword");

        when(authUseCase.login(any(LoginRequest.class)))
                .thenThrow(new LoginFailedException(AuthErrorCode.LOGIN_FAILED, "Too many failed login attempts"));
        String requestBody = objectMapper.writeValueAsString(request);


        for (int i = 0; i < 4; i++) {

            doNothing().when(loginAttemptService).validateIpNotBlocked(testIp);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType("application/json")
                            .content(requestBody)
                            .header("X-Forwarded-For", testIp)) // Simulate client IP
                    .andExpect(status().isUnauthorized());

        }

        doThrow(new TooManyAttemptsException(AuthErrorCode.TOO_MANY_ATTEMPTS, testIp, 5, 15, "IP " + testIp + "가 차단되었습니다."))
                .when(loginAttemptService).validateIpNotBlocked(testIp);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody)
                        .header("X-Forwarded-For", testIp)) // Simulate client IP
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "900"));

        verify(loginAttemptService, times(4)).recordFailedAttempt(testIp);
    }

}
