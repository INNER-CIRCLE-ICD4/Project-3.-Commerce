package commerce.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder, @Value("${jwt.authority-claim:roles}") String authorityClaim) {

        JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName(authorityClaim);
        gac.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(gac);

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource(null)))
                .authorizeExchange(ex ->
                        ex
                                .pathMatchers(HttpMethod.POST, "/api/member-service/members").permitAll()
                                .pathMatchers(
                                        "/api/member-service/auth/**",
                                        "/actuator/**",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/swagger-ui.html"
                                ).permitAll()

                                // 판매자/구매자 공통
                                .pathMatchers("/api/member-service/members/**").hasAnyRole("BUYER", "SELLER", "ADMIN")

                                // 판매자 전용
                                .anyExchange().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder)
                                .jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(delegate))
                        )
                );

        return http.build();
    }

    @Bean
    public ServerAccessDeniedHandler accessDeniedHandler() {

        return (exchange, denied) -> {

            var res = exchange.getResponse();
            res.setStatusCode(HttpStatus.FORBIDDEN);
            res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String traceId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
            String body = """
                    {"success":false,"code":"AUTH-403","message":"Forbidden","traceId":"%s","timestamp":%d}
                    """.formatted(traceId == null ? "" : traceId, System.currentTimeMillis());

            return res.writeWith(Mono.just(res.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
        };
    }

    @Bean
    public ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return ((exchange, ex) -> {
            var res = exchange.getResponse();
            res.setStatusCode(HttpStatus.UNAUTHORIZED);
            res.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String traceId = MDC.get("traceId");
            String spanId = MDC.get("spanId");

            String body = """
                    {"success":false,"code":"AUTH-401","message":"Unauthorized","traceId":"%s", "spanId": "%s","timestamp":%d}
                    """.formatted(traceId == null ? "" : traceId, spanId, System.currentTimeMillis());

            return res.writeWith(Mono.just(res.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
        });
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder(
            @Value("${jwt.secret:}") String secret,
            @Value("${jwt.jwk-set-uri:}") String jwkSetUri
    ) {

        // 🔍 실제 secret 값 디버깅
        log.info("🔑 Gateway JWT Secret 전체: '{}'", secret);
        log.info("🔑 Gateway JWT Secret 길이: {}", secret.length());
        log.info("🔑 Gateway JWT Secret 바이트 길이: {}", secret.getBytes(StandardCharsets.UTF_8).length);

        if (secret.isEmpty()) {
            log.warn("❌ JWT Secret이 비어있습니다!");
            throw new IllegalArgumentException("JWT secret cannot be empty");
        }

        if (org.springframework.util.StringUtils.hasText(jwkSetUri)) {
            return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }

        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

        // 🔍 생성된 키 정보 확인
        log.info("🔧 SecretKey Algorithm: {}", key.getAlgorithm());
        log.info("🔧 SecretKey Format: {}", key.getFormat());
        log.info("✅ JWT Decoder 생성 완료 (HS512)");

        return NimbusReactiveJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${cors.allowed-origins:*}") String allowedOriginsCsv
    ) {
        List<String> origins = Arrays.asList(allowedOriginsCsv.split("\\s*,\\s*"));

        CorsConfiguration config = new CorsConfiguration();
        // * 와 allowCredentials 조합 문제 해결
        if (origins.size() == 1 && "*".equals(origins.get(0))) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(origins);
        }
        config.setAllowCredentials(true);
        config.addAllowedHeader(CorsConfiguration.ALL);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
