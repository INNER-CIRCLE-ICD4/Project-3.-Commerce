package innercircle.global.auth;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode {

    // 인증 관련
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH-01", "Login Failed","로그인에 실패하였습니다. 이메일과 비밀번호를 확인해주세요."),
    ;

    private final HttpStatus status;
    private final String errorCode;
    private final String title;
    private final String userMessage;

    AuthErrorCode(HttpStatus status, String errorCode, String title, String userMessage) {
        this.errorCode = errorCode;
        this.status = status;
        this.title = title;
        this.userMessage = userMessage;
    }
}
