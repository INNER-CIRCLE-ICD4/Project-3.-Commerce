package innercircle.member.domain.member;

import innercircle.global.GlobalException;
import innercircle.global.member.MemberErrorCode;
import org.springframework.http.HttpStatus;

public class DuplicateRequestException extends GlobalException {

    public DuplicateRequestException(MemberErrorCode errorCode, String message) {
        super(errorCode.getStatus(), errorCode.getErrorCode(), errorCode.getTitle(), errorCode.getUserMessage(), message);
    }
}
