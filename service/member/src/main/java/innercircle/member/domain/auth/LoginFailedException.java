package innercircle.member.domain.auth;

import innercircle.global.ErrorCode;
import innercircle.global.GlobalException;
import org.springframework.http.HttpStatus;

public class LoginFailedException extends GlobalException {

    public LoginFailedException(ErrorCode code, String errorMessage) {
        super(code.getStatus(), code.getErrorCode(), code.getTitle(), code.getUserMessage(), errorMessage);
    }
}
