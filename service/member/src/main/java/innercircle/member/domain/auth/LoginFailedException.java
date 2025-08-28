package innercircle.member.domain.auth;

import innercircle.global.auth.AuthErrorCode;
import innercircle.global.GlobalException;

public class LoginFailedException extends GlobalException {

    public LoginFailedException(AuthErrorCode code, String errorMessage) {
        super(code.getStatus(), code.getErrorCode(), code.getTitle(), code.getUserMessage(), errorMessage);
    }
}
