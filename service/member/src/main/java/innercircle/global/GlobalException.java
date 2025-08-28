package innercircle.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class GlobalException extends RuntimeException {

    private HttpStatus status;
    private String errorCode;
    private String title;
    private String userMessage;

    public GlobalException(HttpStatus status, String errorCode,String title, String userMessage ,String message) {
        super(message);
        this.status = status;
        this.title = title;
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
}
