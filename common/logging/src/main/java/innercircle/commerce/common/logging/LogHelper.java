package innercircle.commerce.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Slf4j
public class LogHelper {

    private static final Logger bizLogger = LoggerFactory.getLogger("biz");

    public static void info(String message, Object... args) {
        bizLogger.info("[{}][{}] " + message,
                MDC.get("traceId"),
                MDC.get("userId") != null ? MDC.get("userId") : "-",
                args);
    }

    public static void warn(String message, Object... args) {
        bizLogger.warn("[{}][{}] " + message,
                MDC.get("traceId"),
                MDC.get("userId") != null ? MDC.get("userId") : "-",
                args);
    }

    public static void error(String message, Throwable t) {
        bizLogger.error("[{}][{}] " + message,
                MDC.get("traceId"),
                MDC.get("userId") != null ? MDC.get("userId") : "-",
                t);
    }
}
