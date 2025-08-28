package innercircle.member.application.service;

import innercircle.member.application.port.out.PasswordEncoderPort;
import innercircle.member.application.port.out.TokenPort;
import innercircle.member.application.port.out.UserAuthInfoProvider;
import innercircle.member.domain.auth.AuthToken;
import innercircle.member.domain.auth.UserAuthInfo;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginRequest;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.RefreshRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @InjectMocks
    private AuthApplicationService authApplicationService;

    @Mock
    private UserAuthInfoProvider userAuthInfoProvider;

    @Mock
    private TokenPort tokenPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;


    @Test
    void login_성공_유효한_사용자() {

        UserAuthInfo userAuthInfo = UserAuthInfo.create(1L, "sw@gmail.com", "encodedPassword1234", List.of("BUYER"), true);
        AuthToken authToken = new AuthToken("accessToken_sw", "refreshToken_sw", "Bearer", 3600L);
        LoginRequest request = new LoginRequest("sw@gmail.com", "commerce1234");

        when(userAuthInfoProvider.findByEmail(any(String.class))).thenReturn(userAuthInfo);
        when(passwordEncoderPort.matches(request.password(), userAuthInfo.getEncodedPassword())).thenReturn(Boolean.TRUE);
        when(tokenPort.generateTokenPair(userAuthInfo.getUserId(), userAuthInfo.getEmail(), userAuthInfo.getRoles())).thenReturn(authToken);

        AuthToken result = authApplicationService.login(request);

        assertThat(authToken).isEqualTo(result);

        verify(userAuthInfoProvider).findByEmail("sw@gmail.com");
        verify(passwordEncoderPort).matches(request.password(), userAuthInfo.getEncodedPassword());
        verify(tokenPort).generateTokenPair(userAuthInfo.getUserId(), userAuthInfo.getEmail(), userAuthInfo.getRoles());
    }


    @Test
    void refresh_성공_유효한_리프레시_토큰() {


        RefreshRequest request = new RefreshRequest("refreshToken_sw");
        UserAuthInfo userAuthInfo = UserAuthInfo.create(1L, "sw@gmail.com", "encodedPassword1234", List.of("BUYER"), true);
        String emailFromToken = "sw@gmail.com";
        AuthToken authToken = new AuthToken("accessToken_sw", "refreshToken_sw", "Bearer", 3600L);


        when(tokenPort.validateToken(request.refreshToken())).thenReturn(Boolean.TRUE);
        when(tokenPort.isRefreshToken(request.refreshToken())).thenReturn(Boolean.TRUE);
        when(tokenPort.getEmailFromToken(request.refreshToken())).thenReturn("sw@gmail.com");
        when(userAuthInfoProvider.findByEmail(emailFromToken)).thenReturn(userAuthInfo);
        when(tokenPort.generateTokenPair(userAuthInfo.getUserId(), userAuthInfo.getEmail(), userAuthInfo.getRoles())).thenReturn(authToken);


        AuthToken result = authApplicationService.refresh(request);

        assertThat(result).isEqualTo(authToken);

        verify(tokenPort).validateToken(request.refreshToken());
        verify(tokenPort).isRefreshToken(request.refreshToken());
        verify(tokenPort).getEmailFromToken(request.refreshToken());
        verify(userAuthInfoProvider).findByEmail(emailFromToken);
        verify(tokenPort).generateTokenPair(userAuthInfo.getUserId(), userAuthInfo.getEmail(), userAuthInfo.getRoles());
    }



}