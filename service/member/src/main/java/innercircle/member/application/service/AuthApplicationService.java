package innercircle.member.application.service;

import innercircle.global.auth.AuthErrorCode;
import innercircle.member.application.port.in.AuthUseCase;
import innercircle.member.application.port.out.PasswordEncoderPort;
import innercircle.member.application.port.out.TokenPort;
import innercircle.member.application.port.out.UserAuthInfoProvider;
import innercircle.member.domain.auth.AuthToken;
import innercircle.member.domain.auth.LoginFailedException;
import innercircle.member.domain.auth.UserAuthInfo;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginRequest;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.RefreshRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthApplicationService implements AuthUseCase {

    private final UserAuthInfoProvider userAuthInfoProvider;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenPort tokenPort;

    @Override
    public AuthToken login(LoginRequest request) {

        UserAuthInfo userInfo = userAuthInfoProvider.findByEmail(request.email());

        if (!passwordEncoderPort.matches(request.password(), userInfo.getEncodedPassword())) {
            throw new LoginFailedException(AuthErrorCode.LOGIN_FAILED, "Password mismatch for user: " + userInfo.getEmail());
        }

        return tokenPort.generateTokenPair(userInfo.getUserId(), userInfo.getEmail(), userInfo.getRoles());
    }

    @Override
    public AuthToken refresh(RefreshRequest request) {

        if (!tokenPort.validateToken(request.refreshToken())) {
            throw new IllegalArgumentException("Invalid refresh token: " + request.refreshToken());
        }

        if (!tokenPort.isRefreshToken(request.refreshToken())) {
            throw new IllegalArgumentException("The provided token is not a refresh token: " + request.refreshToken());
        }

        String emailFromToken = tokenPort.getEmailFromToken(request.refreshToken());

        UserAuthInfo userAuthInfo = userAuthInfoProvider.findByEmail(emailFromToken);

        return tokenPort.generateTokenPair(
                userAuthInfo.getUserId(),
                userAuthInfo.getEmail(),
                userAuthInfo.getRoles()
        );
    }

}
