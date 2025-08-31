package innercircle.global;

import innercircle.member.domain.auth.TooManyAttemptsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail exception(Exception e) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", e.getClass().getSimpleName());

        return problemDetail;
    }

    @ExceptionHandler(GlobalException.class)
    public ProblemDetail handleGlobalException(GlobalException e) {
        log.warn("Handled GlobalException: {}", e.getMessage());
        return defaultProblemDetail(e);
    }

    /**
     * 브루트포스 차단 예외 처리
     */
    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ProblemDetail> handleTooManyAttempts(
            TooManyAttemptsException e,
            HttpServletRequest request) {

        log.warn("🚨 IP 차단: ip={}, 시도횟수={}, 잠금시간={}분",
                e.getClientIp(), e.getAttemptCount(), e.getLockTimeMinutes());


        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(e.getLockTimeMinutes() * 60))
                .body(defaultProblemDetail(e));
    }

    private static ProblemDetail defaultProblemDetail(GlobalException exception) {
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
