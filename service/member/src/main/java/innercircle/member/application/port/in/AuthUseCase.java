package innercircle.member.application.port.in;

import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginRequest;
import innercircle.member.infrastructure.adapter.in.web.auth.dto.LoginResponse;

public interface AuthUseCase {

    LoginResponse login(LoginRequest request);

}
