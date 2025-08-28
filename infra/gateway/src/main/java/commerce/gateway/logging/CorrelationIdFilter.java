package commerce.gateway.logging;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = extractOrGenerateCorrelationId(exchange.getRequest());

        if (correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Correlation-Id", correlationId)
                .build();

        exchange.getResponse().getHeaders().add("X-Correlation-Id", correlationId);

        return chain.filter(exchange.mutate().request(request).build());
    }

    private String extractOrGenerateCorrelationId(ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst("X-Correlation-Id");
        return (correlationId != null && !correlationId.isBlank())
                ? correlationId
                : UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
