# 🚀 Commerce Gateway Service

## 📋 개요

Commerce 플랫폼의 **중앙 API Gateway**입니다. Spring Cloud Gateway 기반으로 MSA 환경에서 **모든 외부 요청의 단일 진입점**을 제공하며, **JWT 인증**, **분산 추적**, **보안 관리**를 통합적으로 처리합니다.

![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-4.1.0-brightgreen?logo=spring)
![Micrometer Tracing](https://img.shields.io/badge/Micrometer%20Tracing-1.2.0-blue)
![Zipkin](https://img.shields.io/badge/Zipkin-2.24-orange)
![JWT](https://img.shields.io/badge/JWT-0.12.3-purple)

---

## ✨ 핵심 기능

### 🔐 **JWT 인증 시스템**
- **완전한 JWT 라이프사이클**: 토큰 검증, 블랙리스트, 로그아웃
- **실시간 토큰 무효화**: 로그아웃 시 즉시 토큰 차단
- **메모리 기반 블랙리스트**: 빠른 응답 속도 보장
- **자동 정리**: 만료된 토큰 주기적 제거

### 🔍 **완전한 분산 추적**
- **Micrometer Tracing**: 자동 Span 생성 및 전파
- **Zipkin 연동**: 실시간 요청 플로우 시각화
- **서비스 간 연결**: Gateway → Member → DB 전체 추적
- **성능 분석**: 구간별 소요 시간 측정

### 🛡️ **보안 & 사용자 컨텍스트**
- **자동 사용자 정보 전파**: 인증된 사용자 정보를 헤더로 전송
- **역할 기반 접근 제어**: BUYER, SELLER, ADMIN 권한 관리
- **브루트포스 방어**: Member Service와 연동한 IP 차단
- **CORS 처리**: 프론트엔드 요청 허용

### 🌐 **스마트 라우팅**
- **동적 경로 변환**: `/api/member-service/**` → `/api/v1/**`
- **로드 밸런싱**: 서비스 인스턴스 간 요청 분산
- **헬스체크 연동**: 장애 서비스 자동 제외

---

## 🏗️ 시스템 아키텍처

### **전체 플로우**
```mermaid
graph TD
    Client[Client Apps] --> Gateway[Gateway :8000]
    Gateway --> Member[Member Service :8080]
    Gateway --> Order[Order Service :8081] 
    Gateway --> Product[Product Service :8082]
    
    Gateway --> Zipkin[Zipkin :9411]
    Member --> Zipkin
    Order --> Zipkin
    Product --> Zipkin
    
    Member --> DB1[(Member DB)]
    Order --> DB2[(Order DB)]
    Product --> DB3[(Product DB)]
    
    style Gateway fill:#e1f5fe
    style Zipkin fill:#fff3e0
```

### **JWT 인증 플로우**
```mermaid
sequenceDiagram
    participant C as Client
    participant G as Gateway
    participant M as Member Service
    participant Z as Zipkin
    
    C->>G: POST /auth/login
    G->>M: 사용자 인증 요청
    M->>G: JWT 토큰 반환
    G->>C: 로그인 성공 (토큰)
    
    Note over G: JWT 블랙리스트 확인
    C->>G: API 요청 (+ JWT)
    G->>G: 토큰 검증 & 블랙리스트 체크
    G->>M: 인증된 요청 전달
    
    C->>G: POST /auth/logout
    G->>G: JWT 블랙리스트 추가
    G->>M: 로그아웃 처리
    G->>C: 로그아웃 완료
    
    G->>Z: 모든 요청 추적 데이터
    M->>Z: 서비스 처리 추적 데이터
```

---

## 🚀 빠른 시작

### **1. 사전 요구사항**
- **Java 21** 이상
- **Docker** (Zipkin 실행용)
- **Member Service** 실행 중

### **2. 환경 변수 설정**
```bash
# JWT 시크릿 키 (256비트 이상)
export JWT_SECRET="mySecretKey123456789012345678901234567890123456789012345678901234567890"

# Zipkin 엔드포인트 (선택사항)
export ZIPKIN_ENDPOINT="http://localhost:9411/api/v2/spans"
```

### **3. Zipkin 서버 실행**
```bash
# Docker Compose로 Zipkin 실행
cd infra/gateway
docker-compose up zipkin -d

# 또는 직접 실행
docker run -d -p 9411:9411 openzipkin/zipkin
```

### **4. Gateway 실행**
```bash
# 개발 환경 (local profile)
cd infra/gateway
./gradlew bootRun

# 운영 환경 (prod profile)
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### **5. 동작 확인**

**헬스체크:**
```bash
curl http://localhost:8000/actuator/health
```

**완전한 인증 플로우 테스트:**
```bash
# 1. 로그인 (JWT 토큰 획득)
TOKEN=$(curl -s -X POST http://localhost:8000/api/member-service/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}' | jq -r '.accessToken')

# 2. 인증된 요청
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/api/member-service/members/me

# 3. 로그아웃 (토큰 블랙리스트 추가)
curl -X POST http://localhost:8000/api/member-service/auth/logout \
  -H "Authorization: Bearer $TOKEN"

# 4. 로그아웃된 토큰으로 재요청 (401 에러 예상)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/api/member-service/members/me
```

**Zipkin 추적 확인:**
```bash
# Zipkin UI 접속
open http://localhost:9411

# 요청 후 Traces에서 전체 플로우 확인 가능
```

---

## 🛠️ 핵심 설정

### **🔐 JWT 인증 설정**
```yaml
jwt:
  secret: ${JWT_SECRET:local-dev-secret-change-me}
  authority-claim: roles
  principal-claim: email
```

### **📊 분산 추적 설정**
```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0    # 개발: 100%, 운영: 0.1
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
  observations:
    web:
      server:
        enabled: true

spring:
  reactor:
    context-propagation: AUTO  # WebFlux 컨텍스트 전파
```

### **🌐 라우팅 설정**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: member-service
          uri: http://localhost:8080           # 개발환경
          # uri: http://member-service:8080    # 운영환경
          predicates:
            - Path=/api/member-service/**
          filters:
            - RewritePath=/api/member-service/(?<segment>.*), /api/v1/$\{segment}
```

**경로 변환 예시:**
- `POST /api/member-service/auth/login` → `POST http://localhost:8080/api/v1/auth/login`
- `GET /api/member-service/members/me` → `GET http://localhost:8080/api/v1/members/me`

### **🛡️ CORS 설정**
```yaml
cors:
  allowed-origins: "http://localhost:3000,http://localhost:5173"
  allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
  allowed-headers: "*"
  allow-credentials: true
```

---

## 📝 API 엔드포인트

### **🔓 공개 API (인증 불필요)**
| Method | Endpoint | 설명 | 전달 위치 |
|--------|----------|------|-----------|
| `POST` | `/api/member-service/auth/login` | 로그인 | Member Service |
| `POST` | `/api/member-service/auth/refresh` | 토큰 갱신 | Member Service |
| `POST` | `/api/member-service/members` | 회원가입 | Member Service |
| `GET` | `/actuator/health` | Gateway 상태 | Gateway |

### **🔐 인증 필요 API**
| Method | Endpoint | 설명 | 필요 권한 |
|--------|----------|------|-----------|
| `GET` | `/api/member-service/members/me` | 내 프로필 | BUYER+ |
| `PUT` | `/api/member-service/members/me` | 프로필 수정 | BUYER+ |
| `POST` | `/api/member-service/auth/logout` | 로그아웃 | BUYER+ |
| `GET` | `/api/order-service/**` | 주문 관련 | BUYER+ |
| `GET` | `/api/product-service/**` | 상품 조회 | BUYER+ |

### **👑 관리자 전용 API**
| Method | Endpoint | 설명 | 필요 권한 |
|--------|----------|------|-----------|
| `GET` | `/api/member-service/admin/members` | 회원 목록 | ADMIN |
| `PUT` | `/api/member-service/admin/members/{id}` | 회원 상태 변경 | ADMIN |
| `POST` | `/api/product-service/admin/products` | 상품 등록 | ADMIN |

### **🔍 분산 추적 API**
| Method | Endpoint | 설명 | 응답 헤더 |
|--------|----------|------|-----------|
| 모든 요청 | `/**` | 자동 추적 | `X-Trace-Id`, `X-Span-Id` |

---

## 🔍 완전한 분산 추적 시스템

### **🎯 Micrometer Tracing + Zipkin**

**자동 추적 대상:**
- ✅ **HTTP 요청/응답**: Gateway ↔ Backend Services
- ✅ **데이터베이스 쿼리**: JPA, JDBC 자동 추적
- ✅ **JWT 토큰 검증**: 인증 처리 시간 측정
- ✅ **비즈니스 로직**: Service 메서드 호출
- ✅ **에러 및 예외**: 실패 원인 자동 태깅

### **📊 Zipkin UI**
```bash
# Zipkin 대시보드 접속
http://localhost:9411

# 추적 검색
- Service: gateway, member-service
- Span Name: GET /api/member-service/members/me
- Tags: http.status_code=200, error=false
```

### **🏷️ 자동 생성 태그**
```yaml
# HTTP 관련
http.method: POST
http.url: /api/member-service/auth/login
http.status_code: 200

# 사용자 관련 (UserContextFilter 추가)
user.id: 123456789
user.email: test@test.com
user.roles: BUYER,SELLER

# 에러 관련 (실패 시)
error: true
error.type: LoginFailedException
error.message: 비밀번호가 일치하지 않습니다
```

### **📈 성능 분석**
```
📊 Trace Example: 로그인 요청
├── gateway: POST /api/member-service/auth/login (500ms)
│   ├── JWT 검증 (50ms)
│   ├── 블랙리스트 확인 (5ms)
│   └── Member Service 호출 (445ms)
│       ├── 인증 처리 (200ms)
│       ├── 데이터베이스 조회 (150ms)
│       └── JWT 토큰 생성 (95ms)
└── 응답 전송 (10ms)
```

### **🔄 자동 컨텍스트 전파**
모든 요청에 추적 헤더 자동 전파:
```http
# Gateway → Member Service 전송 헤더
X-Trace-Id: abc123def456789a
X-Span-Id: def456789abc1234
B3-TraceId: abc123def456789a
B3-SpanId: def456789abc1234
```

**로그 출력:**
```
[gateway,abc123def456789a,def456789abc1234] INFO - 로그인 요청 수신
[member-service,abc123def456789a,ghi789abc123def4] INFO - 사용자 인증 처리
```

---

## 🔒 JWT 블랙리스트 시스템

### **🎯 로그아웃 시 토큰 무효화**

**기존 문제:**
- JWT는 stateless → 로그아웃 후에도 토큰 유효
- 토큰 탈취 시 만료까지 악용 가능

**해결책:**
- **메모리 기반 블랙리스트**: 로그아웃된 토큰 실시간 차단
- **자동 정리**: 만료된 토큰 주기적 제거
- **빠른 성능**: O(1) 조회 시간

### **🔄 동작 플로우**

```java
// 1. 로그아웃 요청 시
POST /api/member-service/auth/logout
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

// 2. Gateway에서 JTI 추출 후 블랙리스트 추가
jwtBlacklistService.blacklistToken(jti, expirationTime);

// 3. 이후 같은 토큰으로 요청 시
if (jwtBlacklistService.isBlacklisted(jti)) {
    return 401 Unauthorized; // 즉시 차단
}
```

### **⚡ 성능 최적화**

**메모리 관리:**
```java
// 15분마다 만료된 토큰 자동 정리
@Scheduled(fixedRate = 15 * 60 * 1000)
private void cleanupExpiredTokens() {
    // ConcurrentHashMap에서 만료된 항목 제거
}

// 현재 블랙리스트 상태 모니터링
public Map<String, Object> getStats() {
    return Map.of(
        "totalBlacklistedTokens", blacklistedTokens.size(),
        "lastCleanupTime", lastCleanupTime
    );
}
```

**모니터링:**
```bash
# 블랙리스트 상태 확인
curl http://localhost:8000/actuator/metrics/jwt.blacklist.size
```

---

## 🧪 테스트 가이드

### **🔧 단위 테스트**
```bash
# Gateway 단위 테스트
cd infra/gateway
./gradlew test

# JWT 블랙리스트 서비스 테스트
./gradlew test --tests "*JwtBlacklistServiceTest"

# 사용자 컨텍스트 필터 테스트
./gradlew test --tests "*UserContextFilterTest"
```

### **🌐 통합 테스트**
```bash
# Gateway + Member Service 연동 테스트
./gradlew integrationTest

# 전체 인증 플로우 테스트 (로그인 → 인증 → 로그아웃)
curl -X POST http://localhost:8000/api/member-service/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}'
```

### **🔍 분산 추적 테스트**
```bash
# 1. Zipkin 서버 실행 확인
curl http://localhost:9411/health

# 2. 추적 가능한 요청 전송
curl -X GET http://localhost:8000/api/member-service/members/me \
  -H "Authorization: Bearer $TOKEN"

# 3. Zipkin UI에서 추적 확인
open http://localhost:9411
```

### **🔒 JWT 블랙리스트 테스트**
```bash
# 1. 로그인 (토큰 획득)
TOKEN=$(curl -s -X POST http://localhost:8000/api/member-service/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}' | jq -r '.accessToken')

# 2. 인증된 요청 (성공 예상)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/api/member-service/members/me

# 3. 로그아웃 (블랙리스트 추가)
curl -X POST http://localhost:8000/api/member-service/auth/logout \
  -H "Authorization: Bearer $TOKEN"

# 4. 동일 토큰으로 재요청 (401 예상)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/api/member-service/members/me

# 5. 블랙리스트 상태 확인
curl http://localhost:8000/actuator/health
```

### **📊 성능 테스트**
```bash
# Apache Bench로 동시 요청 테스트
ab -n 1000 -c 10 -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/api/member-service/members/me

# JMeter 스크립트 실행
jmeter -n -t gateway-performance-test.jmx -l results.jtl
```

---

## 📚 관련 문서

### **🔐 인증 & 보안**
- 📖 [JWT 인증 가이드](docs/JWT_AUTHENTICATION_GUIDE.md) - 완전한 JWT 시스템 구현 가이드
- 🔒 [Refresh Token 가이드](../../service/member/docs/REFRESH_TOKEN_GUIDE.md) - 토큰 갱신 시스템

### **🔍 분산 추적**
- 📊 [분산 추적 가이드](docs/DISTRIBUTED_TRACING_GUIDE.md) - Micrometer + Zipkin 완전 가이드
- 🎯 [분산 추적 구현 보고서](../../service/search/SEARCH_ENGINE_IMPLEMENTATION_REPORT.md)

### **🌐 라우팅 & 게이트웨이**
- 🛣️ [Gateway 라우팅 가이드](docs/GATEWAY_ROUTING_GUIDE.md)
- 🏗️ [Gateway 구현 계획서](docs/GATEWAY_IMPLEMENTATION_PLAN.md)
- 🔍 [서비스 디스커버리 비교](docs/service-discovery-comparison.md)

---

## 🛡️ 운영 보안 가이드

### **🔑 JWT 보안**
```yaml
# 프로덕션 권장 설정
jwt:
  secret: ${JWT_SECRET}  # 최소 256비트 (64자) 이상
  access-token-expiry: 900000      # 15분
  refresh-token-expiry: 604800000  # 7일
```

**보안 체크리스트:**
- ✅ **강력한 시크릿**: 64자 이상 랜덤 문자열
- ✅ **짧은 만료 시간**: AccessToken 15분 이하
- ✅ **HTTPS 전용**: 프로덕션에서 TLS 필수
- ✅ **정기 로테이션**: 시크릿 키 정기 변경

### **🌐 CORS 보안**
```yaml
# 프로덕션 설정
cors:
  allowed-origins: "https://commerce.example.com"  # 특정 도메인만
  allowed-methods: "GET,POST,PUT,DELETE"          # 필요한 메서드만
  allow-credentials: true                         # 쿠키 전송 허용
  max-age: 3600                                  # 프리플라이트 캐시
```

### **🔒 블랙리스트 보안**
- **메모리 제한**: 최대 블랙리스트 크기 설정
- **정리 주기**: 15분마다 만료 토큰 제거
- **모니터링**: 비정상적 증가 감지

---

## 🚀 배포 & 운영

### **🐳 Docker 배포**
```bash
# 이미지 빌드
docker build -t commerce-gateway:latest .

# 컨테이너 실행
docker run -d \
  --name gateway \
  -p 8000:8000 \
  -e JWT_SECRET="production-secret-key" \
  -e SPRING_PROFILES_ACTIVE=prod \
  commerce-gateway:latest
```

### **📊 모니터링 & 알림**

**핵심 메트릭:**
```yaml
# Prometheus 메트릭
- http_server_requests_total      # 총 요청 수
- http_server_requests_duration   # 응답 시간
- jwt_blacklist_size             # 블랙리스트 크기
- zipkin_spans_total             # 추적 span 수
```
---

## 🏆 성능 최적화

### **⚡ 응답 시간 개선**
- **Connection Pooling**: HTTP 클라이언트 연결 풀 최적화
- **JWT 캐싱**: 검증된 토큰 결과 임시 캐싱
- **비동기 처리**: WebFlux 논블로킹 I/O 활용

### **🔧 튜닝 가이드**
```yaml
# application.yml 성능 튜닝
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 500
          max-idle-time: 30s
        connect-timeout: 3000
        response-timeout: 10s
```

---

## 🆘 트러블슈팅

### **자주 발생하는 문제**

**1. JWT 검증 실패**
```bash
# 원인: 시크릿 키 불일치
# 해결: Gateway와 Member Service JWT_SECRET 확인
echo $JWT_SECRET
```

**2. 분산 추적 연결 끊김**
```bash
# 원인: Zipkin 서버 다운
# 해결: Zipkin 서버 상태 확인
curl http://localhost:9411/health
```

**3. CORS 에러**
```bash
# 원인: 허용되지 않은 Origin
# 해결: allowed-origins 설정 확인
```

---

**Tech Stack:**
- ![Spring Cloud Gateway](https://img.shields.io/badge/Gateway-4.1.0-brightgreen)
- ![Micrometer](https://img.shields.io/badge/Tracing-1.2.0-blue)  
- ![Zipkin](https://img.shields.io/badge/Zipkin-2.24-orange)
- ![JWT](https://img.shields.io/badge/JWT-0.12.3-purple)

**서비스 정보:**
- **Port**: 8000
- **Profiles**: local, prod
- **Version**: 1.0.0
- **Last Updated**: 2025-08-28

**개발팀**: Commerce Platform Team  
**Repository**: [GitHub - Commerce Gateway](https://github.com/INNER-CIRCLE-ICD4/Project-3.-Commerce/tree/main/infra/gateway)
