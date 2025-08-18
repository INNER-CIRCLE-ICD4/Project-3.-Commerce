package innercircle.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail exception(Exception e) {
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    @ExceptionHandler(GlobalException.class)
    public ProblemDetail handleGlobalException(GlobalException e) {

        return getProblemDetail(e);
    }

    private static ProblemDetail getProblemDetail(HttpStatus status, Exception exception) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());

        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }

    private static ProblemDetail getProblemDetail(GlobalException exception) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
        problemDetail.setTitle(exception.getTitle());
        problemDetail.setDetail(exception.getUserMessage());
        problemDetail.setProperty("errorCode", exception.getErrorCode());
        problemDetail.setProperty("errorMessage", exception.getMessage());
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }
}
