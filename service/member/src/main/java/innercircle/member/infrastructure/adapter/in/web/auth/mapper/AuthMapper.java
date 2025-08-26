package innercircle.member.infrastructure.adapter.in.web.auth.mapper;

import innercircle.member.domain.auth.AuthToken;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginResponse;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.RefreshResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public RefreshResponse authTokenToRefreshResponse(AuthToken authToken) {
        return new RefreshResponse(
                authToken.accessToken(),
                authToken.refreshToken(),
                authToken.tokenType(),
                authToken.expiresIn()
        );
    }


    public LoginResponse authTokenToLoginResponse(AuthToken authToken) {
        return new LoginResponse(
                authToken.accessToken(),
                authToken.refreshToken(),
                authToken.tokenType(),
                authToken.expiresIn()
        );
    }
}
