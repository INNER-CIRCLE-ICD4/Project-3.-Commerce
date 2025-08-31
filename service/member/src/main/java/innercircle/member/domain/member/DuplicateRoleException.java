package innercircle.member.domain.member;

import innercircle.global.GlobalException;
import innercircle.global.member.MemberErrorCode;

public class DuplicateRoleException extends GlobalException {

    public DuplicateRoleException(MemberErrorCode errorCode, String message) {
        super(errorCode.getStatus(), errorCode.getErrorCode(), errorCode.getTitle(), errorCode.getUserMessage(), message);
    }
}
