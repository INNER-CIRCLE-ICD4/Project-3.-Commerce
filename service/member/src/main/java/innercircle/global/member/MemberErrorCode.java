package innercircle.global.member;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode {

    // 인증 관련
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "MEM-01", "Duplicate Email","중복된 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEM-02", "Not Found", "유저를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String errorCode;
    private final String title;
    private final String userMessage;

    MemberErrorCode(HttpStatus status, String errorCode, String title, String userMessage) {
        this.errorCode = errorCode;
        this.status = status;
        this.title = title;
        this.userMessage = userMessage;
    }
}
