# Gateway 라우팅 설정 가이드

## 📋 개요
Spring Cloud Gateway의 RewritePath 필터를 사용한 서비스 연결 설정 가이드

---

## 🎯 핵심 개념

### RewritePath 패턴
```yaml
filters:
  - RewritePath=/gateway-prefix/(?<segment>.*), /service-context-path/$\{segment}
```

**동작 과정:**
1. 클라이언트 요청: `GET /gateway-prefix/auth/login`
2. 정규표현식 매칭: `(?<segment>.*)` → `segment=auth/login`
3. 경로 변환: `/service-context-path/auth/login`
4. 백엔드 서비스 호출

---

## 🔧 실제 설정 예시

### Member Service 연결
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: member-service
          uri: http://localhost:8080
          predicates:
            - Path=/api/member-service/**
          filters:
            - RewritePath=/api/member-service/(?<segment>.*), /api/v1/$\{segment}
```

**변환 예시:**
| 클라이언트 요청 | segment 값 | 백엔드 호출 |
|---|---|---|
| `/api/member-service/auth/login` | `auth/login` | `/api/v1/auth/login` |
| `/api/member-service/members/123` | `members/123` | `/api/v1/members/123` |
| `/api/member-service/profile` | `profile` | `/api/v1/profile` |

### 다른 서비스 예시
```yaml
routes:
  # Order Service
  - id: order-service
    uri: http://localhost:8081
    predicates:
      - Path=/api/order-service/**
    filters:
      - RewritePath=/api/order-service/(?<segment>.*), /api/v1/$\{segment}
  
  # Product Service  
  - id: product-service
    uri: http://localhost:8082
    predicates:
      - Path=/api/product-service/**
    filters:
      - RewritePath=/api/product-service/(?<segment>.*), /api/v2/$\{segment}
```

---

## ⚠️ 주의사항

### 1. SecurityConfig 화이트리스트 매칭
```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(ex -> ex
                .pathMatchers(
                    "/api/member-service/auth/**",    // ✅ Gateway 경로 기준
                    "/api/member-service/members",    // ✅ 변환 전 경로
                    "/api/order-service/public/**"
                ).permitAll()
                .anyExchange().authenticated()
            )
            .build();
    }
}
```

### 2. 경로 매칭 우선순위
```yaml
routes:
  # 구체적인 경로가 먼저 (우선순위 높음)
  - id: member-auth
    uri: http://localhost:8080
    predicates:
      - Path=/api/member-service/auth/**
    filters:
      - RewritePath=/api/member-service/auth/(?<segment>.*), /auth/$\{segment}
  
  # 일반적인 경로가 나중에 (우선순위 낮음)  
  - id: member-service
    uri: http://localhost:8080
    predicates:
      - Path=/api/member-service/**
    filters:
      - RewritePath=/api/member-service/(?<segment>.*), /api/v1/$\{segment}
```

---

## 🧪 테스트 방법

### 1. 직접 서비스 호출
```bash
# Member Service 직접 호출 (context-path 포함)
curl http://localhost:8080/api/v1/members

# 응답: 200 OK
```

### 2. Gateway를 통한 호출
```bash
# Gateway를 통한 호출
curl http://localhost:8000/api/member-service/members

# 내부적으로: /api/member-service/members → /api/v1/members
# 응답: 200 OK (동일한 결과)
```

### 3. 라우팅 로그 확인
```yaml
logging:
  level:
    org.springframework.cloud.gateway.filter.RewritePathGatewayFilterFactory: DEBUG
```

**로그 출력:**
```
DEBUG o.s.c.g.filter.RewritePathGatewayFilterFactory : 
  Rewrite /api/member-service/auth/login to /api/v1/auth/login
```

---

## 🚀 패턴별 활용 예시

### 1. 동일한 Context-Path
```yaml
# 모든 서비스가 /api/v1 사용
- RewritePath=/api/(?<service>.*)-service/(?<segment>.*), /api/v1/$\{segment}
```

### 2. 서비스별 다른 Context-Path
```yaml
# Member: /api/v1, Order: /api/v2
- id: member-service
  filters:
    - RewritePath=/api/member-service/(?<segment>.*), /api/v1/$\{segment}
- id: order-service  
  filters:
    - RewritePath=/api/order-service/(?<segment>.*), /api/v2/$\{segment}
```

### 3. StripPrefix 대안
```yaml
# RewritePath 사용 (권장)
- RewritePath=/api/member-service/(?<segment>.*), /api/v1/$\{segment}

# StripPrefix 사용 (단순한 경우만)
- StripPrefix=2  # /api/member-service 제거
```

---

## 💡 모범 사례

1. **일관된 네이밍**: `/api/{service-name}-service/**` 패턴 유지
2. **명확한 segment**: 정규표현식 그룹명을 명확하게 지정
3. **보안 고려**: Gateway 경로 기준으로 화이트리스트 설정
4. **로그 활용**: RewritePath 디버그 로그로 변환 과정 확인

---

**작성일**: 2025-01-11  
**최종 수정**: Gateway 팀
