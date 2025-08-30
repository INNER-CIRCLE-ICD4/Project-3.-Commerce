package commerce.gateway.security;

import commerce.gateway.ratelimit.RateLimitConfig;
import commerce.gateway.ratelimit.RateLimitResult;
import commerce.gateway.ratelimit.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final RateLimitService rateLimitService;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        log.error("🚨 RateLimitingFilter.filter() 호출됨!"); // 강제 에러 로그로 확실히 보이게

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();

        RateLimitConfig config = getRateLimitConfig(path);

        return generateRateLimitKey(exchange)
                .flatMap(key -> {
                    RateLimitResult result = rateLimitService.checkLimit(key, config);
                    if (result.isAllowed()) {
                        // 허용된 경우
                        addRateLimitHeaders(exchange, result);
                        log.debug("Rate limit  통과: {} {} key = {}, remaining={}/{}", method, path, key, result.getRemainingTokens(), result.getLimitCapacity());
                        return chain.filter(exchange);
                    } else {
                        // 차단된 경우
                        log.warn("Rate limit  초과: {} {} key = {}, 용량 초과 (남은 토큰: 0), 재시도 가능 시간: {}초", method, path, key, result.getRetryAfterSeconds());

                        return createRateLimitExceededResponse(exchange, result);
                    }
                });
    }

    @Override
    public int getOrder() {
        return -50; //UserContextFilter(LOWEST_PRECEDENCE)보다 먼저 실행하여 불필요한 JWT 검증 방지
    }

    /**
     * 🎯 경로별 Rate Limit 설정 결정
     */
    private RateLimitConfig getRateLimitConfig(String path) {
        if (path.contains("/auth/")) {
            return RateLimitConfig.forAuth();      // 로그인: 엄격 (2/초, 10 버스트)
        } else if (path.contains("/admin/")) {
            return RateLimitConfig.forAdmin();     // 관리자: 중간 (10/초, 30 버스트)
        } else if (path.contains("/actuator/health")) {
            return RateLimitConfig.forHealth();    // 헬스체크: 관대 (100/초, 200 버스트)
        } else {
            return RateLimitConfig.forGeneral();   // 일반: 완화 (50/초, 100 버스트)
        }
    }

    /**
     * Rate Limit 키 생성(IP + 사용자 조합)
     */
    private Mono<String> generateRateLimitKey(ServerWebExchange exchange) {

        String clientIp = getClientIp(exchange);

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .cast(JwtAuthenticationToken.class)
                .map(auth -> {
                    String userId = auth.getToken().getSubject();
                    return "user: " + userId;
                })
                .switchIfEmpty(Mono.just("ip:" + clientIp));
    }

    /**
     * 클라이언트 IP 추출
     */
    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return Objects.requireNonNull(
                exchange.getRequest().getRemoteAddress()
        ).getAddress().getHostAddress();
    }

    /**
     * Rate Limit 헤더 추가(정상 응답 시)
     */
    private void addRateLimitHeaders(ServerWebExchange exchange, RateLimitResult result) {
        exchange.getResponse().getHeaders().set("X-RateLimit-Limit", String.valueOf(result.getLimitCapacity()));
        exchange.getResponse().getHeaders().set("X-RateLimit-Remaining", String.valueOf(result.getRemainingTokens()));
    }

    /**
     * 🚫 Rate Limit 헤더 추가(차단 응답 시)
     */
    private Mono<Void> createRateLimitExceededResponse(ServerWebExchange exchange, RateLimitResult result) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().set("Content-Type",
                MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8");
        exchange.getResponse().getHeaders().set("X-RateLimit-Limit",
                String.valueOf(result.getLimitCapacity()));
        exchange.getResponse().getHeaders().set("X-RateLimit-Remaining", "0");
        exchange.getResponse().getHeaders().set("Retry-After",
                String.valueOf(result.getRetryAfterSeconds()));

        String errorBody = String.format("""
                {
                    "success": false,
                    "code": "RATE_LIMIT_EXCEEDED",
                    "message": "요청 횟수가 제한을 초과했습니다",
                    "limit": %d,
                    "retryAfter": %d,
                    "timestamp": %d
                }
                """, result.getLimitCapacity(), result.getRetryAfterSeconds(), System.currentTimeMillis());

        var buffer = exchange.getResponse().bufferFactory()
                .wrap(errorBody.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

}
