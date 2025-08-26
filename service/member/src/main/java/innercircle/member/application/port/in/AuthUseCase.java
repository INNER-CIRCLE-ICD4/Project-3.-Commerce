package innercircle.member.application.port.in;

import innercircle.member.domain.auth.AuthToken;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginRequest;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.RefreshRequest;

public interface AuthUseCase {

    AuthToken login(LoginRequest request);

    AuthToken refresh(RefreshRequest request);
}
