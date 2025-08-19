package innercircle.member.infrastructure.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import innercircle.commerce.common.snowflake.Snowflake;
import innercircle.config.SecurityConfig;
import innercircle.global.auth.AuthErrorCode;
import innercircle.member.application.port.in.AuthUseCase;
import innercircle.member.application.port.out.TokenPort;
import innercircle.member.domain.auth.LoginFailedException;
import innercircle.member.domain.auth.LoginRequest;
import innercircle.member.domain.auth.LoginResponse;
import innercircle.member.infrastructure.adapter.out.JwtTokenAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class})
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthUseCase authUseCase;

    TokenPort tokenPort;

    @BeforeEach
    public void setUp() {
        // Any setup code can go here if needed
        String safeSecret = "mySecretKey123456789012345678901234567890abcdefghijklmnopqrstuvwxyz12345";
        tokenPort = new JwtTokenAdapter(
                safeSecret,           // 64자 = 256비트 이상
                3600000L,            // 1시간
                604800000L           // 7일
        );

    }


    @Test
    void login() throws Exception {

        LoginRequest loginRequest = new LoginRequest("sw.noh@gmail.com", "password1234");

        long userId = new Snowflake().nextId();
        String accessToken = tokenPort.generateAccessToken(userId, "sw.noh@gmail.com", List.of("BUYER"));
        String refreshToken = tokenPort.generateRefreshToken(userId, "sw.noh@gmail.com", List.of("BUYER"));

        LoginResponse loginResponse = new LoginResponse(accessToken, refreshToken, "Bearer", 3600000L);

        when(authUseCase.login(loginRequest))
                .thenReturn(loginResponse);


        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));

    }

    @Test
    void login_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {


        LoginRequest loginRequest = new LoginRequest("sw.noh@gmail.com", "password1234");

        when(authUseCase.login(loginRequest))
                .thenThrow(new LoginFailedException(AuthErrorCode.LOGIN_FAILED, "비밀번호가 일치하지 않습니다."));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.title").value("Login Failed"))
                .andExpect(jsonPath("$.detail").value("로그인에 실패하였습니다. 이메일과 비밀번호를 확인해주세요."));

    }


}