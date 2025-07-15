package innercircle.commerce.common.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


@Component
@ComponentScan(basePackages = "innercircle.commerce")
public class LoggingFilter implements Filter {

    private final String serviceName;

    public LoggingFilter(Environment environment) {
        this.serviceName = environment.getProperty("spring.application.name", "unknown-service");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String traceId = Optional.ofNullable( httpRequest.getHeader("X-Trace-Id") )
                    .orElse( UUID.randomUUID().toString() );

            MDC.put("traceId", traceId);
            MDC.put("service", serviceName);

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

}