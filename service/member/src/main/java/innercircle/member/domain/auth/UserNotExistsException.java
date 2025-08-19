package innercircle.member.domain.auth;

import innercircle.global.auth.AuthErrorCode;
import innercircle.global.GlobalException;

public class UserNotExistsException extends GlobalException {
    public UserNotExistsException(AuthErrorCode code, String message) {
        super(code.getStatus(), code.getErrorCode(), code.getTitle(), code.getUserMessage(), message);
    }
}
