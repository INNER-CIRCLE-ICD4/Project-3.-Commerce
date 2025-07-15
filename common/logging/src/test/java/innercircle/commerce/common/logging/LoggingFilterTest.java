package innercircle.commerce.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoggingFilterTest {

    @Test
    void saveTraceIdAndServiceName2MDC() throws IOException, ServletException {
        // given
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.application.name", "test-service");

        LoggingFilter filter = new LoggingFilter(env);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("X-Trace-Id")).thenReturn(null); // traceId 없음

        ServletResponse mockResponse = mock(ServletResponse.class);

        // FilterChain 내에서 MDC 값 확인
        FilterChain mockChain = (req, res) -> {
            assertThat(MDC.get("traceId")).isNotBlank();
            assertThat(MDC.get("service")).isEqualTo("test-service");
        };

        // when
        filter.doFilter(mockRequest, mockResponse, mockChain);
    }
}
