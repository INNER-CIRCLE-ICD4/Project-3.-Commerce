package innercircle.member.domain.auth;

import innercircle.global.ErrorCode;
import innercircle.global.GlobalException;
import org.springframework.http.HttpStatus;

public class UserNotExistsException extends GlobalException {
    public UserNotExistsException(ErrorCode code, String message) {
        super(code.getStatus(), code.getErrorCode(), code.getTitle(), code.getUserMessage(), message);
    }
}
