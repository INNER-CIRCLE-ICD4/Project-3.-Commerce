package innercircle.member.application.service;

import innercircle.global.auth.AuthErrorCode;
import innercircle.member.application.port.in.AuthUseCase;
import innercircle.member.application.port.out.PasswordEncoderPort;
import innercircle.member.application.port.out.TokenPort;
import innercircle.member.application.port.out.UserAuthInfoProvider;
import innercircle.member.domain.auth.LoginFailedException;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginRequest;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginResponse;
import innercircle.member.domain.auth.UserAuthInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthApplicationService implements AuthUseCase {

    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final UserAuthInfoProvider userAuthInfoProvider;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenPort tokenPort;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Override
    public LoginResponse login(LoginRequest request) {
        UserAuthInfo userInfo = userAuthInfoProvider.findByEmail(request.email());

        if (!passwordEncoderPort.matches(request.password(), userInfo.getEncodedPassword())) {
            throw new LoginFailedException(AuthErrorCode.LOGIN_FAILED, "Password mismatch for user: " + userInfo.getEmail());
        }

        //jwt 토큰 생성
        return issueTokenResponse(userInfo);
    }


    /**
     * JWT 토큰 발급 및 응답 생성
     * access 토큰 및 refresh 토큰
     * @param userInfo
     * @return
     */
    private LoginResponse issueTokenResponse(UserAuthInfo userInfo) {
        String accessToken = tokenPort.generateAccessToken(
                userInfo.getUserId(),
                userInfo.getEmail(),
                userInfo.getRoles()
        );

        String refreshTokenToken = tokenPort.generateRefreshToken(
                userInfo.getUserId(),
                userInfo.getEmail(),
                userInfo.getRoles()
        );

        return new LoginResponse(
                accessToken,
                refreshTokenToken,
                TOKEN_TYPE_BEARER,
                accessTokenExpiry
        );
    }
}
