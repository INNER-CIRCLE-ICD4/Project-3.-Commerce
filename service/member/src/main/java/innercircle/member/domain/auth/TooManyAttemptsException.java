package innercircle.member.domain.auth;

import innercircle.global.GlobalException;
import innercircle.global.auth.AuthErrorCode;
import lombok.Getter;

@Getter
public class TooManyAttemptsException extends GlobalException {

    private String clientIp;
    private int attemptCount;
    private int lockTimeMinutes;

    public TooManyAttemptsException(AuthErrorCode code, String clientIp, int attemptCount, int lockTimeMinutes,  String errorMessage) {
        super(code.getStatus(), code.getErrorCode(), code.getTitle(), code.getUserMessage(), errorMessage);
        this.clientIp = clientIp;
        this.attemptCount = attemptCount;
        this.lockTimeMinutes = lockTimeMinutes;
    }
}
