package innercircle.commerce.common.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;

class LoggingHelperTest {

    @BeforeEach
    void setUp() {
        MDC.put("traceId", "test-trace-id");
        MDC.put("userId", "user-123");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void infoTest() {
        Assertions.assertThatCode(() -> LogHelper.info("상품 조회 요청: id={}", 12))
                .doesNotThrowAnyException();
    }

    @Test
    void warnTest() {
        Assertions.assertThatCode(() -> LogHelper.warn("위험한 요청: path={}", "/test"))
                .doesNotThrowAnyException();
    }

    @Test
    void errorTest() {
        Assertions.assertThatCode(() -> LogHelper.error("예외 발생", new RuntimeException("err")))
                .doesNotThrowAnyException();
    }

    @Test
    void LogMessageFormatTest() {
        // 1. StringAppender 또는 ListAppender 등 설정
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger("biz");
        logger.addAppender(listAppender);

        // 2. 실제 호출
        MDC.put("traceId", "abc123");
        MDC.put("userId", "user42");
        LogHelper.info("상품 조회 요청: id={}", 99);

        // 3. 로그 메시지 확인
        List<ILoggingEvent> logs = listAppender.list;
        Assertions.assertThat(logs).hasSize(1);
        Assertions.assertThat(logs.get(0).getFormattedMessage())
                .contains("[abc123][user42] 상품 조회 요청: id=[99]");

        listAppender.stop();
    }
}
