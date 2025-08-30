package commerce.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static java.util.Date.from;
import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class UserContextFilter implements GlobalFilter, Ordered {

    private final String principalClaim;
    private final JwtBlacklistService jwtBlacklistService;

    public UserContextFilter(@Value("${jwt.principal-claim:sub}") String principalClaim, JwtBlacklistService jwtBlacklistService) {
        this.principalClaim = principalClaim;
        this.jwtBlacklistService = jwtBlacklistService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");

        // 🔥 이 로깅으로 추적
        log.info("🌐 [{}] {} {} -> Routing to Member Service",
                traceId != null ? traceId.substring(0, 8) : "unknown",
                method,
                path);

        log.info("=== UserContextFilter STARTED ===");
        log.info("Request method: " + exchange.getRequest().getMethod());

        if (exchange.getRequest().getPath().value().equals("/api/v1/auth/logout")) {
            return handleLogout(exchange, chain);
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .cast(JwtAuthenticationToken.class)
                .switchIfEmpty(Mono.empty())
                .flatMap(auth -> {
                    String jti = auth.getToken().getClaimAsString("jti");
                    if (jti != null && jwtBlacklistService.isBlacklisted(jti)) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                        String body = """
                                {"success":false,"code":"AUTH-401","message":"로그아웃된 토큰입니다","timestamp":%d}
                                """.formatted(System.currentTimeMillis());
                        return exchange.getResponse().writeWith(
                                Mono.just(
                                        exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))
                                )
                        );
                    }

                    return processAuthenticatedRequest(auth, exchange, chain);
                })
                .switchIfEmpty(processUnauthenticatedRequest(exchange, chain));
    }

    /**
     * 🔐 인증된 요청 처리
     */
    private Mono<Void> processAuthenticatedRequest(JwtAuthenticationToken auth, ServerWebExchange exchange, GatewayFilterChain chain) {
        String userId = auth.getToken().getSubject();
        String email = auth.getToken().getClaimAsString(principalClaim);
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));


        // 🔥 이 1줄만 추가: URL 정보 로깅
        log.info("🎯 {} {} -> {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath().value(),
                email);

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(h -> {
                    h.set("X-User-ID", userId);
                    h.set("X-EMAIL", email);
                    h.set("X-ROLES", roles);
                    h.set("X-AUTH-METHOD", "JWT");
                })
                .build();

        ServerWebExchange modifiedExchange = exchange.mutate().request(mutated).build();
        removeInternalHeaders(modifiedExchange);
        return chain.filter(modifiedExchange);
    }

    /**
     * 🌐 인증되지 않은 요청 처리
     */
    private Mono<Void> processUnauthenticatedRequest(ServerWebExchange exchange, GatewayFilterChain chain) {
        removeInternalHeaders(exchange);
        return chain.filter(exchange);
    }

    private void removeInternalHeaders(ServerWebExchange exchange) {
        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();

        // 🚨 클라이언트에게 노출되면 안 되는 내부 헤더들 제거
        responseHeaders.remove("X-User-ID");
        responseHeaders.remove("X-EMAIL");
        responseHeaders.remove("X-ROLES");
        responseHeaders.remove("X-AUTH-METHOD");

        log.info("🚨 내부 헤더 제거됨: X-User-ID, X-EMAIL, X-ROLES, X-AUTH-METHOD");
    }

    /**
     * 🚪 로그아웃 처리
     */
    private Mono<Void> handleLogout(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .cast(JwtAuthenticationToken.class)
                .flatMap(auth -> {
                    // ✅ JWT에서 JTI 추출하여 블랙리스트에 추가
                    String jti = auth.getToken().getClaimAsString("jti");
                    String email = auth.getToken().getSubject();

                    if (jti != null) {
                        jwtBlacklistService.blacklistToken(jti, from(requireNonNull(auth.getToken().getExpiresAt())));

                        log.info("🚪 로그아웃 토큰 블랙리스트 추가: email={}, , JTI={}", email, jti.substring(0, Math.min(8, jti.length())));

                    } else {
                        log.warn("⚠️ 토큰에 JTI가 없습니다: email={}", email);
                    }
                    return createLogoutSuccessResponse(exchange, email);
                })
                .onErrorResume(throwable -> {
                    log.error("⚠️ 로그아웃 처리 중 오류", throwable);
                    return createLogoutSuccessResponse(exchange, "error"); // 오류가 있어도 로그아웃 진행
                });
    }

    private Mono<Void> createLogoutSuccessResponse(ServerWebExchange exchange, String email) {
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        String responseBody = String.format("""
                {
                    "success": true,
                    "message": "로그아웃되었습니다",
                    "timestamp": %d
                }
                """, System.currentTimeMillis());

        log.info("✅ 로그아웃 완료: email={}", email);

        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(responseBody.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
