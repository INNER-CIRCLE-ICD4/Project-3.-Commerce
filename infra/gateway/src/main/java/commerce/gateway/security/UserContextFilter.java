package commerce.gateway.security;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
public class UserContextFilter implements GlobalFilter, Ordered {

    private final String principalClaim;

    public UserContextFilter(@Value("${jwt.principal-claim:sub}") String principalClaim) {
        this.principalClaim = principalClaim;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("=== UserContextFilter STARTED ===");
        System.out.println("Request path: " + exchange.getRequest().getPath());
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .cast(Authentication.class)
                .map(auth -> {
                    if (auth instanceof JwtAuthenticationToken token) {
                        String email = token.getToken().getClaimAsString(principalClaim);
                        String roles = auth.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(","));

                        ServerHttpRequest mutated = exchange.getRequest().mutate()
                                .headers(h -> {
                                    if (email != null) h.set("X-EMAIL", email);
                                    if (!roles.isEmpty()) h.set("X-ROLES", roles);
                                })
                                .build();
                        return exchange.mutate().request(mutated).build();
                    }

                    return exchange;
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter)
                .doFinally(signalType -> {
                    addTraceHeaders(exchange);
                });
    }

    private void addTraceHeaders(ServerWebExchange exchange) {
        String traceId = MDC.get("traceId");
        String spanId = MDC.get("spanId");

        System.out.println("spanId = " + spanId);
        System.out.println("traceId = " + traceId);

        if (traceId != null) {
            exchange.getResponse().getHeaders().add("X-TRACE-ID", traceId);
        }
        if (spanId != null) {
            exchange.getResponse().getHeaders().add("X-SPAN-ID", spanId);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
