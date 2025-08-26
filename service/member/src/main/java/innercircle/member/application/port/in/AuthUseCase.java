package innercircle.member.application.port.in;

import innercircle.member.domain.auth.LoginRequest;
import innercircle.member.domain.auth.LoginResponse;

public interface AuthUseCase {

    LoginResponse login(LoginRequest request);



}
