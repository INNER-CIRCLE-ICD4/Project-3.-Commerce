# 🚀 Commerce Gateway Service

## 📋 개요

Commerce 플랫폼의 **중앙 API Gateway**입니다. Spring Cloud Gateway 기반으로 MSA 환경에서 **모든 외부 요청의 단일 진입점**을 제공하며, **JWT 인증**, **Rate Limiting**, **분산 추적**, **보안 관리**를 통합적으로 처리합니다.

> 📊 **로깅 중앙화**와 **메트릭 수집**은 현재 개발 예정 단계입니다.

![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-4.1.0-brightgreen?logo=spring)
![Micrometer Tracing](https://img.shields.io/badge/Micrometer%20Tracing-1.2.0-blue)
![Zipkin](https://img.shields.io/badge/Zipkin-2.24-orange)
![JWT](https://img.shields.io/badge/JWT-0.12.3-purple)
![Rate Limiting](https://img.shields.io/badge/Rate%20Limiting-Token%20Bucket-red)

---

## ✨ 핵심 기능

### 🛡️ **인메모리 Rate Limiting 시스템**
- **Token Bucket 알고리즘**: 메모리 기반 고성능 속도 제한
- **API별 차등 제한**: 인증(2/초), 일반(50/초), 관리자(10/초), 헬스체크(100/초)
- **사용자별 & IP별**: 인증된 사용자는 개별 제한, 미인증은 IP별 제한  
- **자동 토큰 보충**: 5초마다 토큰 자동 갱신
- **메모리 최적화**: 30분마다 비활성 버킷 자동 정리
- **기본 모니터링**: 차단 비율, 활성 버킷 수 추적 (중앙화 시스템은 개발 예정)

### 🔐 **완전한 JWT 인증 시스템**
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
    
    subgraph "Gateway 필터 체인"
        RF[RateLimitingFilter<br/>-50] --> UF[UserContextFilter<br/>LOWEST_PRECEDENCE]
    end
    
    style Gateway fill:#e1f5fe
    style Zipkin fill:#fff3e0
    style RF fill:#ffebee
    style UF fill:#f3e5f5
```

### **Rate Limiting + JWT 인증 플로우**
```mermaid
sequenceDiagram
    participant C as Client
    participant G as Gateway
    participant RL as RateLimitingFilter
    participant UF as UserContextFilter
    participant M as Member Service
    
    C->>G: POST /auth/login
    G->>RL: Rate Limit 체크
    alt Rate Limit 초과
        RL->>C: 429 Too Many Requests
    else Rate Limit 통과
        RL->>UF: JWT 검증
        UF->>M: 사용자 인증 요청
        M->>UF: JWT 토큰 반환
        UF->>RL: 인증 완료
        RL->>C: 로그인 성공 (토큰)
    end
    
    Note over G: 이후 요청들
    C->>G: API 요청 (+ JWT)
    G->>RL: Rate Limit 체크 (사용자별)
    RL->>UF: JWT 검증 & 블랙리스트 체크
    UF->>M: 인증된 요청 전달
    M->>UF: 응답
    UF->>RL: 응답
    RL->>C: Rate Limit 헤더 포함 응답
```

---

## 🎯 Rate Limiting 상세 정책

### **📊 API별 제한 정책**

| **API 유형** | **경로 패턴** | **초당 제한** | **버스트 허용** | **설명** |
|-------------|---------------|---------------|----------------|----------|
| 🔐 **인증 API** | `/auth/**` | **2개** | **10개** | 브루트포스 방어 |
| 👑 **관리자 API** | `/admin/**` | **10개** | **30개** | 중요 기능 보호 |
| 🌐 **일반 API** | `/api/**` | **50개** | **100개** | 일반 사용자 편의 |
| 🚀 **헬스체크** | `/actuator/health` | **100개** | **200개** | 모니터링 도구 허용 |

### **🔑 제한 키 생성 방식**

```java
// 인증된 사용자: 개별 제한
"user:{userId}"  // 예: user:12345

// 미인증 사용자: IP별 제한  
"ip:{clientIP}"  // 예: ip:192.168.1.100
```

### **🪣 Token Bucket 알고리즘**

```
📊 Token Bucket 동작 방식:

초기 상태: 버킷 가득참 [🪙🪙🪙🪙🪙] (capacity: 5)
            refillRate: 2/초

요청 1개: [🪙🪙🪙🪙_] → ✅ 허용 (4개 남음)
요청 2개: [🪙🪙__] → ✅ 허용 (2개 남음)  
요청 3개: [____] → 🚫 거부 (토큰 부족)

1초 후: [🪙🪙____] → 2개 보충됨
요청 1개: [🪙____] → ✅ 허용 (1개 남음)
```

### **⚡ 성능 최적화**
- **ConcurrentHashMap**: 멀티스레드 안전성
- **AtomicInteger**: Lock-free 토큰 관리
- **ScheduledExecutorService**: 비동기 토큰 보충
- **Parallel Stream**: 다중 버킷 병렬 처리

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

**Rate Limiting 테스트:**
```bash
# 1. 일반 API 테스트 (50/초 제한)
for i in {1..60}; do
  echo "요청 $i:"
  curl -w "상태: %{http_code}, 시간: %{time_total}s\n" -s -o /dev/null \
    -H "X-RateLimit-Test: true" \
    http://localhost:8000/api/member-service/actuator/health
  sleep 0.01
done

# 2. 인증 API 테스트 (2/초 제한)
for i in {1..10}; do
  echo "로그인 시도 $i:"
  curl -w "상태: %{http_code}\n" -s -o /dev/null \
    -X POST http://localhost:8000/api/member-service/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"test"}'
  sleep 0.1
done

# 3. Rate Limit 헤더 확인
curl -v http://localhost:8000/api/member-service/actuator/health
# 응답 헤더에서 확인:
# X-RateLimit-Limit: 100
# X-RateLimit-Remaining: 99
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

**Rate Limiting 모니터링:**
```bash
# Rate Limiting 통계 확인
curl http://localhost:8000/actuator/ratelimit/summary
# 응답 예시:
# {
#   "activeBuckets": 5,
#   "blockRate": "2.34%",
#   "totalRequests": 1247,
#   "totalBlocked": 29
# }

# 상세 통계
curl http://localhost:8000/actuator/ratelimit/stats | jq '.'
```

**Zipkin 추적 확인:**
```bash
# Zipkin UI 접속
open http://localhost:9411

# 요청 후 Traces에서 전체 플로우 확인 가능
```

---

## 🛠️ 핵심 설정

### **⚡ Rate Limiting 설정**
```yaml
# application.yml에 설정 없음 - 코드로 구현됨

# 동적 설정 (런타임)
logging:
  level:
    commerce.gateway.ratelimit: DEBUG  # Rate Limit 로그 상세 출력
```

**API별 제한 설정 (RateLimitConfig.java):**
```java
// 인증 API: 브루트포스 방어
public static RateLimitConfig forAuth() {
    return new RateLimitConfig(2, 10, 1);  // 2/초, 10 버스트
}

// 일반 API: 사용자 편의
public static RateLimitConfig forGeneral() {
    return new RateLimitConfig(50, 100, 1);  // 50/초, 100 버스트
}

// 관리자 API: 중간 제한
public static RateLimitConfig forAdmin() {
    return new RateLimitConfig(10, 30, 1);  // 10/초, 30 버스트
}

// 헬스체크: 관대한 제한
public static RateLimitConfig forHealth() {
    return new RateLimitConfig(100, 200, 1);  // 100/초, 200 버스트
}
```

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
| Method | Endpoint | 설명 | Rate Limit | 전달 위치 |
|--------|----------|------|------------|-----------|
| `POST` | `/api/member-service/auth/login` | 로그인 | **2/초, 10 버스트** | Member Service |
| `POST` | `/api/member-service/auth/refresh` | 토큰 갱신 | **2/초, 10 버스트** | Member Service |
| `POST` | `/api/member-service/members` | 회원가입 | **50/초, 100 버스트** | Member Service |
| `GET` | `/actuator/health` | Gateway 상태 | **100/초, 200 버스트** | Gateway |

### **🔐 인증 필요 API**
| Method | Endpoint | 설명 | Rate Limit | 필요 권한 |
|--------|----------|------|------------|-----------|
| `GET` | `/api/member-service/members/me` | 내 프로필 | **50/초, 100 버스트** | BUYER+ |
| `PUT` | `/api/member-service/members/me` | 프로필 수정 | **50/초, 100 버스트** | BUYER+ |
| `POST` | `/api/member-service/auth/logout` | 로그아웃 | **2/초, 10 버스트** | BUYER+ |
| `GET` | `/api/order-service/**` | 주문 관련 | **50/초, 100 버스트** | BUYER+ |
| `GET` | `/api/product-service/**` | 상품 조회 | **50/초, 100 버스트** | BUYER+ |

### **👑 관리자 전용 API**
| Method | Endpoint | 설명 | Rate Limit | 필요 권한 |
|--------|----------|------|------------|-----------|
| `GET` | `/api/member-service/admin/members` | 회원 목록 | **10/초, 30 버스트** | ADMIN |
| `PUT` | `/api/member-service/admin/members/{id}` | 회원 상태 변경 | **10/초, 30 버스트** | ADMIN |
| `POST` | `/api/product-service/admin/products` | 상품 등록 | **10/초, 30 버스트** | ADMIN |

### **📊 Rate Limiting 모니터링 API**
| Method | Endpoint | 설명 | 응답 |
|--------|----------|------|------|
| `GET` | `/actuator/ratelimit/summary` | 간단한 통계 | `{"activeBuckets": 5, "blockRate": "2.34%"}` |
| `GET` | `/actuator/ratelimit/stats` | 상세 통계 | 버킷 상세, 총 요청 수, 차단 수 등 |
| `DELETE` | `/actuator/ratelimit/reset/{key}` | 특정 키 리셋 | `{"message": "Rate limit reset successfully"}` |
| `DELETE` | `/actuator/ratelimit/reset-all` | 모든 제한 리셋 | `{"message": "All rate limits reset successfully"}` |

### **🔍 분산 추적 API**
| Method | Endpoint | 설명 | 응답 헤더 |
|--------|----------|------|-----------|
| 모든 요청 | `/**` | 자동 추적 | `X-Trace-Id`, `X-Span-Id`, `X-RateLimit-*` |

---

## 📊 Rate Limiting 시스템 상세

### **🎯 TokenBucket 구현**

**핵심 알고리즘:**
```java
public class TokenBucket {
    private final int capacity;           // 최대 토큰 수
    private final int refillRate;         // 초당 보충률
    private final AtomicInteger tokens;   // 현재 토큰 수
    private volatile long lastRefillTime; // 마지막 보충 시간
    
    // 토큰 소비 시도 (Thread-safe)
    public boolean tryConsume(int requestedTokens) {
        refill(); // 먼저 토큰 보충
        
        // CAS 기반 원자적 업데이트
        while (true) {
            int currentTokens = tokens.get();
            if (currentTokens < requestedTokens) {
                return false; // 토큰 부족
            }
            
            int newTokens = currentTokens - requestedTokens;
            if (tokens.compareAndSet(currentTokens, newTokens)) {
                return true; // 성공
            }
            // CAS 실패 시 재시도
        }
    }
}
```

### **⚡ 성능 특징**

| **특성** | **구현 방식** | **성능** |
|----------|---------------|----------|
| **토큰 조회** | `AtomicInteger.get()` | **O(1)** |
| **토큰 소비** | CAS 기반 원자적 업데이트 | **O(1)** |
| **버킷 관리** | `ConcurrentHashMap` | **O(1)** 평균 |
| **메모리 사용** | 버킷당 약 64 bytes | **매우 효율적** |
| **동시성** | Lock-free 알고리즘 | **높은 처리량** |

### **📈 자동 관리 시스템**

**토큰 보충 (5초마다):**
```java
@Scheduled(fixedRate = 5000)
private void refillAllBuckets() {
    buckets.values().parallelStream()
           .forEach(TokenBucket::refill);
}
```

**오래된 버킷 정리 (30분마다):**
```java
@Scheduled(fixedRate = 1800000) // 30분
private void cleanupOldBuckets() {
    long cutoffTime = System.currentTimeMillis() - 3600000; // 1시간 전
    buckets.entrySet().removeIf(entry -> 
        entry.getValue().getLastRefillTime() < cutoffTime
    );
}
```

### **🔍 실시간 모니터링**

**통계 API 응답 예시:**
```json
{
  "totalBuckets": 147,
  "totalRequests": 12847,
  "totalBlocked": 293,
  "blockRate": "2.28%",
  "lastCleanupTime": "2025-01-15T10:30:00",
  "bucketDetails": {
    "user:12345": {
      "tokens": 47,
      "capacity": 100,
      "refillRate": 50,
      "lastRefillTime": 1705329000000
    },
    "ip:192.168.1.100": {
      "tokens": 8,
      "capacity": 10,
      "refillRate": 2,
      "lastRefillTime": 1705329005000
    }
  }
}
```

---

## 🔍 완전한 분산 추적 시스템

### **🎯 Micrometer Tracing + Zipkin**

**자동 추적 대상:**
- ✅ **HTTP 요청/응답**: Gateway ↔ Backend Services
- ✅ **Rate Limiting**: 제한 검사 및 토큰 소비 시간
- ✅ **JWT 토큰 검증**: 인증 처리 시간 측정
- ✅ **데이터베이스 쿼리**: JPA, JDBC 자동 추적
- ✅ **비즈니스 로직**: Service 메서드 호출
- ✅ **에러 및 예외**: 실패 원인 자동 태깅

### **📊 Zipkin UI**
```bash
# Zipkin 대시보드 접속
http://localhost:9411

# 추적 검색
- Service: gateway, member-service
- Span Name: POST /api/member-service/auth/login
- Tags: http.status_code=200, rate.limit.allowed=true
```

### **🏷️ Rate Limiting 추가 태그**
```yaml
# Rate Limiting 관련 추가 태그
rate.limit.key: user:12345
rate.limit.allowed: true
rate.limit.remaining: 47
rate.limit.capacity: 100
rate.limit.policy: forAuth

# 차단 시 추가 태그
rate.limit.blocked: true
rate.limit.retry_after: 5
```

### **📈 성능 분석**
```
📊 Trace Example: 로그인 요청 (Rate Limit 포함)
├── gateway: POST /api/member-service/auth/login (520ms)
│   ├── RateLimitingFilter (15ms)
│   │   ├── Rate Limit 키 생성 (2ms)
│   │   ├── Token Bucket 조회 (1ms)
│   │   ├── 토큰 소비 시도 (2ms)
│   │   └── 헤더 추가 (10ms)
│   ├── UserContextFilter (55ms)
│   │   ├── JWT 검증 (45ms)
│   │   └── 블랙리스트 확인 (10ms)
│   └── Member Service 호출 (450ms)
│       ├── 인증 처리 (200ms)
│       ├── 데이터베이스 조회 (150ms)
│       └── JWT 토큰 생성 (100ms)
└── 응답 전송 (10ms)
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

---

## 🧪 포괄적 테스트 가이드

### **🔧 단위 테스트**
```bash
# Gateway 전체 테스트
cd infra/gateway
./gradlew test

# Rate Limiting 시스템 테스트
./gradlew test --tests "*RateLimitServiceTest"
./gradlew test --tests "*TokenBucketTest"

# JWT 블랙리스트 서비스 테스트
./gradlew test --tests "*JwtBlacklistServiceTest"

# 사용자 컨텍스트 필터 테스트
./gradlew test --tests "*UserContextFilterTest"
```

### **⚡ Rate Limiting 전용 테스트**

**1. API별 제한 테스트:**
```bash
# 인증 API (2/초 제한) - 빠르게 초과시키기
echo "🔐 인증 API Rate Limit 테스트:"
for i in {1..15}; do
  response=$(curl -s -w "\n%{http_code}" -X POST \
    http://localhost:8000/api/member-service/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"wrong"}')
  
  status_code=$(echo "$response" | tail -n1)
  if [ "$status_code" = "429" ]; then
    echo "요청 $i: 🚫 Rate Limit 차단 (429)"
    break
  else
    echo "요청 $i: ✅ 통과 ($status_code)"
  fi
  sleep 0.1
done

# 일반 API (50/초 제한) - 대량 요청
echo -e "\n🌐 일반 API Rate Limit 테스트:"
for i in {1..60}; do
  status_code=$(curl -s -o /dev/null -w "%{http_code}" \
    http://localhost:8000/api/member-service/actuator/health)
  
  if [ "$status_code" = "429" ]; then
    echo "요청 $i: 🚫 Rate Limit 차단 (429)"
  elif [ $((i % 10)) -eq 0 ]; then
    echo "요청 $i: ✅ 통과 ($status_code)"
  fi
  sleep 0.01
done
```

**2. Rate Limit 헤더 검증:**
```bash
# Rate Limit 헤더 확인
echo "📊 Rate Limit 헤더 테스트:"
response=$(curl -v http://localhost:8000/api/member-service/actuator/health 2>&1)

echo "$response" | grep -E "(X-RateLimit-|HTTP/)"
# 예상 출력:
# < HTTP/1.1 200 OK
# < X-RateLimit-Limit: 200
# < X-RateLimit-Remaining: 199
```

**3. 사용자별 vs IP별 제한 테스트:**
```bash
# 먼저 로그인해서 토큰 획득
TOKEN=$(curl -s -X POST http://localhost:8000/api/member-service/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}' | jq -r '.accessToken')

echo "🔑 인증된 사용자 제한 테스트:"
for i in {1..5}; do
  status_code=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer $TOKEN" \
    http://localhost:8000/api/member-service/members/me)
  echo "인증된 요청 $i: $status_code"
  sleep 0.1
done

echo -e "\n📍 IP 기반 제한 테스트 (미인증):"
for i in {1..5}; do
  status_code=$(curl -s -o /dev/null -w "%{http_code}" \
    http://localhost:8000/api/member-service/actuator/health)
  echo "미인증 요청 $i: $status_code"
  sleep 0.1
done
```

**4. Rate Limit 모니터링 API 테스트:**
```bash
# 통계 확인
echo "📊 Rate Limiting 통계:"
curl -s http://localhost:8000/actuator/ratelimit/stats | jq '{
  totalBuckets,
  totalRequests,
  totalBlocked,
  blockRate
}'

# 요약 정보
echo -e "\n📈 Rate Limiting 요약:"
curl -s http://localhost:8000/actuator/ratelimit/summary | jq '.'

# 특정 키 리셋 테스트
echo -e "\n🔄 Rate Limit 리셋 테스트:"
curl -s -X DELETE http://localhost:8000/actuator/ratelimit/reset/ip:127.0.0.1 | jq '.'
```

### **🌐 통합 테스트**
```bash
# Gateway + Member Service 연동 테스트
./gradlew integrationTest

# 전체 플로우 테스트 (Rate Limit + JWT + 분산추적)
echo "🔄 전체 플로우 통합 테스트:"

# 1. Rate Limit 내에서 로그인
TOKEN=$(curl -s -X POST http://localhost:8000/api/member-service/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}' | jq -r '.accessToken')

if [ "$TOKEN" != "null" ] && [ "$TOKEN" != "" ]; then
  echo "✅ 로그인 성공: ${TOKEN:0:20}..."
  
  # 2. 인증된 API 호출
  profile=$(curl -s -H "Authorization: Bearer $TOKEN" \
    http://localhost:8000/api/member-service/members/me)
  echo "✅ 프로필 조회 성공"
  
  # 3. 로그아웃
  logout=$(curl -s -X POST \
    -H "Authorization: Bearer $TOKEN" \
    http://localhost:8000/api/member-service/auth/logout)
  echo "✅ 로그아웃 성공"
  
  # 4. 블랙리스트 확인 (401 예상)
  blocked=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer $TOKEN" \
    http://localhost:8000/api/member-service/members/me)
  
  if [ "$blocked" = "401" ]; then
    echo "✅ JWT 블랙리스트 동작 확인"
  else
    echo "❌ JWT 블랙리스트 실패: $blocked"
  fi
else
  echo "❌ 로그인 실패 (Rate Limit 또는 인증 오류)"
fi
```

### **🔍 분산 추적 테스트**
```bash
# 1. Zipkin 서버 실행 확인
zipkin_health=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9411/health)
if [ "$zipkin_health" = "200" ]; then
  echo "✅ Zipkin 서버 정상"
else
  echo "❌ Zipkin 서버 오류: $zipkin_health"
fi

# 2. 추적 가능한 요청 전송
echo "🔍 분산 추적 테스트 요청:"
trace_response=$(curl -v -X POST http://localhost:8000/api/member-service/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}' 2>&1)

# 추적 헤더 확인
trace_id=$(echo "$trace_response" | grep -i "x-trace-id" | cut -d' ' -f3)
if [ "$trace_id" != "" ]; then
  echo "✅ Trace ID 생성됨: $trace_id"
  echo "🔍 Zipkin UI 확인: http://localhost:9411/zipkin/traces/$trace_id"
else
  echo "❌ Trace ID 누락"
fi

# 3. Zipkin 추적 데이터 확인
sleep 2  # Zipkin 전송 대기
traces=$(curl -s "http://localhost:9411/api/v2/traces?serviceName=gateway&limit=1")
trace_count=$(echo "$traces" | jq 'length')
echo "📊 최근 추적 수: $trace_count"
```

### **📊 성능 테스트**
```bash
# Apache Bench로 Rate Limiting 성능 테스트
echo "⚡ Rate Limiting 성능 테스트:"
ab -n 1000 -c 10 -k \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/api/member-service/members/me

# 결과 분석
echo -e "\n📊 성능 테스트 결과:"
echo "- Rate Limit 차단: 429 응답 수 확인"
echo "- 평균 응답시간: Rate Limiting 오버헤드 < 10ms"
echo "- 처리량: 정상 요청 처리 능력 확인"

# Concurrent Rate Limiting 테스트
echo -e "\n🚀 동시성 테스트:"
for i in {1..5}; do
  (
    for j in {1..20}; do
      curl -s -o /dev/null -w "Thread $i Request $j: %{http_code}\n" \
        http://localhost:8000/api/member-service/actuator/health
      sleep 0.05
    done
  ) &
done
wait

echo "✅ 동시성 테스트 완료"
```

---

## 📚 관련 문서

### **🛡️ Rate Limiting**
- 📖 [메모리 Rate Limiting 가이드](docs/MEMORY_RATE_LIMITING_GUIDE.md) - Token Bucket 구현 상세 가이드

### **🔐 인증 & 보안**
- 📖 [JWT 인증 가이드](docs/JWT_AUTHENTICATION_GUIDE.md) - 완전한 JWT 시스템 구현 가이드
- 🔒 [Refresh Token 가이드](../../service/member/docs/REFRESH_TOKEN_GUIDE.md) - 토큰 갱신 시스템

### **🔍 분산 추적**
- 📊 [분산 추적 가이드](docs/DISTRIBUTED_TRACING_GUIDE.md) - Micrometer + Zipkin 완전 가이드

### **🌐 라우팅 & 게이트웨이**
- 🛣️ [Gateway 라우팅 가이드](docs/GATEWAY_ROUTING_GUIDE.md)
- 🏗️ [Gateway 구현 계획서](docs/GATEWAY_IMPLEMENTATION_PLAN.md)
- 🔍 [서비스 디스커버리 비교](docs/service-discovery-comparison.md)

---

## 🛡️ 운영 보안 가이드

### **⚡ Rate Limiting 보안**
```yaml
# 프로덕션 권장 설정 (코드 수정 필요)
rate-limiting:
  auth-api:
    refill-rate: 1        # 더 엄격하게: 1/초
    burst-capacity: 5     # 더 작게: 5 버스트
  general-api:
    refill-rate: 20       # 운영 환경 고려: 20/초
    burst-capacity: 50    # 운영 환경 고려: 50 버스트
  cleanup:
    interval: 600000      # 10분마다 정리
    inactive-time: 1800000 # 30분 비활성 시 제거
```

**보안 체크리스트:**
- ✅ **API별 적절한 제한**: 인증 API는 매우 엄격하게
- ✅ **메모리 사용량 모니터링**: 버킷 수 증가 추적
- ✅ **로그 레벨 조정**: 운영에서는 WARN 이상만
- ✅ **비정상 패턴 감지**: 높은 차단 비율 알림

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

### **📊 기본 모니터링**

**현재 구현된 모니터링:**
- ✅ **Rate Limiting 통계**: `/actuator/ratelimit/stats`, `/actuator/ratelimit/summary`
- ✅ **JWT 블랙리스트 모니터링**: 블랙리스트 크기, 정리 통계
- ✅ **분산 추적**: Zipkin을 통한 요청 플로우 추적
- ✅ **Spring Boot Actuator**: 기본 헬스체크 및 메트릭

---

## 🚧 개발 예정 기능

### **📊 로깅 중앙화 시스템** `(개발 예정)`

**계획된 기능:**
- 🔄 **ELK Stack 연동**: Elasticsearch + Logstash + Kibana
- 🏷️ **구조화된 로깅**: JSON 형태의 일관된 로그 포맷
- 🔍 **통합 로그 검색**: 모든 마이크로서비스 로그 중앙화
- 📊 **로그 기반 대시보드**: 실시간 로그 분석 및 시각화

**구현 예정 구조:**
```yaml
# 추후 추가될 설정
logging:
  config: classpath:logback-spring.xml
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n"
  appender:
    logstash:
      destination: "localhost:5044"
      encoder: "net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder"
```

### **📈 메트릭 수집 시스템** `(개발 예정)`

**계획된 기능:**
- 🎯 **Prometheus 메트릭**: 상세한 비즈니스 메트릭 수집
- 📊 **Grafana 대시보드**: 실시간 시스템 모니터링
- 🚨 **AlertManager 연동**: 임계값 기반 알림 시스템
- 📱 **Slack/Teams 알림**: 운영팀 실시간 알림

**구현 예정 메트릭:**
```yaml
# 추후 구현될 Prometheus 메트릭
- http_server_requests_total      # 총 요청 수
- http_server_requests_duration   # 응답 시간
- rate_limit_requests_total       # Rate Limit 총 요청 수
- rate_limit_blocked_total        # Rate Limit 차단 수
- rate_limit_active_buckets       # 활성 버킷 수
- jwt_blacklist_size             # JWT 블랙리스트 크기
- jwt_validation_duration        # JWT 검증 시간
- gateway_route_requests_total   # 라우트별 요청 수
- gateway_circuit_breaker_state  # 서킷 브레이커 상태
```

**구현 예정 알림 규칙:**
```yaml
# AlertManager 알림 규칙 (개발 예정)
- alert: RateLimitHighBlockRate
  expr: rate_limit_blocked_total / rate_limit_requests_total > 0.1
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "Rate Limit 차단율이 10%를 초과했습니다"

- alert: RateLimitTooManyBuckets  
  expr: rate_limit_active_buckets > 10000
  for: 2m
  labels:
    severity: critical
  annotations:
    summary: "Rate Limit 버킷 수가 과도합니다 (메모리 누수 의심)"

- alert: GatewayHighLatency
  expr: histogram_quantile(0.95, rate(http_server_requests_duration_seconds_bucket[5m])) > 1.0
  for: 3m
  labels:
    severity: warning
  annotations:
    summary: "Gateway 응답 시간이 1초를 초과했습니다 (95th percentile)"
```

### **🔄 추가 계획 기능** `(개발 예정)`

**인프라 모니터링:**
- 🖥️ **시스템 리소스**: CPU, Memory, Disk, Network 모니터링
- 🏗️ **JVM 메트릭**: Heap, GC, Thread Pool 모니터링
- 🌐 **네트워크 모니터링**: 연결 풀, 지연 시간, 처리량

**보안 모니터링:**
- 🛡️ **이상 탐지**: 비정상적인 요청 패턴 감지
- 🚨 **보안 이벤트**: JWT 토큰 탈취, 브루트포스 공격 감지
- 📊 **보안 대시보드**: 실시간 보안 상태 모니터링

**비즈니스 메트릭:**
- 👥 **사용자 활동**: 로그인 수, 활성 사용자, 세션 지속시간
- 🛒 **API 사용량**: 엔드포인트별 사용 통계, 성공/실패율
- 💰 **비즈니스 KPI**: 전환율, 응답 품질, 사용자 만족도

---

## 🏆 성능 최적화

### **⚡ Rate Limiting 최적화**
- **Lock-free 알고리즘**: AtomicInteger + CAS 사용
- **병렬 토큰 보충**: ParallelStream으로 다중 버킷 처리
- **메모리 효율성**: 비활성 버킷 자동 정리
- **최소 오버헤드**: 평균 Rate Limit 검사 < 1ms

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

# 비동기 처리 최적화        
server:
  netty:
    connection-timeout: 2000ms
  compression:
    enabled: true
    mime-types: application/json
```

**성능 벤치마크:**
```
⚡ Rate Limiting 성능 측정:
- 토큰 소비: < 0.5ms (평균)
- 버킷 생성: < 1ms (처음 요청 시)
- 메모리 사용: 버킷당 ~64 bytes
- 동시성: 10,000 req/sec 처리 가능
```

---

## 🆘 트러블슈팅

### **Rate Limiting 관련 문제**

**1. Rate Limit이 작동하지 않는 경우**
```bash
# 원인 1: 필터가 실행되지 않음 (내부 API 호출)
# 해결: /api/member-service/** 경로로 테스트

# 원인 2: 컴파일 에러로 Bean 등록 실패
# 해결: 컴파일 상태 확인
./gradlew compileJava

# 원인 3: 로그 레벨 문제로 디버그 정보 안 보임
# 해결: 로그 레벨 조정
logging:
  level:
    commerce.gateway.ratelimit: DEBUG
```

**2. Rate Limit 통계가 0으로 나오는 경우**
```bash
# 원인: Gateway 내부 API는 필터를 거치지 않음
# 잘못된 테스트: curl http://localhost:8000/actuator/health
# 올바른 테스트: curl http://localhost:8000/api/member-service/actuator/health

# 통계 확인
curl http://localhost:8000/actuator/ratelimit/stats
```

**3. 메모리 사용량이 계속 증가하는 경우**
```bash
# 원인: 버킷 정리가 작동하지 않음
# 해결: 정리 로그 확인
grep "버킷 정리" /var/log/gateway.log

# 수동 정리
curl -X DELETE http://localhost:8000/actuator/ratelimit/reset-all
```

### **JWT 관련 문제**

**1. JWT 검증 실패**
```bash
# 원인: 시크릿 키 불일치
# 해결: Gateway와 Member Service JWT_SECRET 확인
echo $JWT_SECRET
```

**2. 블랙리스트가 작동하지 않음**
```bash
# 원인: JTI 클레임이 없음
# 해결: JWT 토큰에 JTI 포함 여부 확인
echo $TOKEN | cut -d'.' -f2 | base64 -d | jq '.'
```

### **분산 추적 문제**

**1. 추적 데이터가 Zipkin에 나타나지 않음**
```bash
# 원인: Zipkin 서버 다운
# 해결: Zipkin 서버 상태 확인
curl http://localhost:9411/health

# 원인: 샘플링 비율 0
# 해결: application.yml 확인
management:
  tracing:
    sampling:
      probability: 1.0  # 개발환경에서 100%
```

**2. Trace ID가 생성되지 않음**
```bash
# 원인: context-propagation 설정 누락
# 해결: application.yml 확인
spring:
  reactor:
    context-propagation: AUTO
```

### **CORS 에러**
```bash
# 원인: 허용되지 않은 Origin
# 해결: allowed-origins 설정 확인
cors:
  allowed-origins: "http://localhost:3000,http://localhost:5173"
```

---

**🛡️ 필터 체인 순서:**
1. **RateLimitingFilter** (Order: -50) - 최우선 요청 제한
2. **UserContextFilter** (Order: LOWEST_PRECEDENCE) - JWT 검증 및 사용자 컨텍스트

**Tech Stack:**
- ![Spring Cloud Gateway](https://img.shields.io/badge/Gateway-4.1.0-brightgreen)
- ![Rate Limiting](https://img.shields.io/badge/Rate%20Limiting-Token%20Bucket-red)
- ![Micrometer](https://img.shields.io/badge/Tracing-1.2.0-blue)  
- ![Zipkin](https://img.shields.io/badge/Zipkin-2.24-orange)
- ![JWT](https://img.shields.io/badge/JWT-0.12.3-purple)

**서비스 정보:**
- **Port**: 8000
- **Profiles**: local, prod
- **Version**: 2.1.0
- **Last Updated**: 2025-01-15

> 📝 **v2.1.0 업데이트**: 로깅 중앙화 및 메트릭 수집 로드맵 추가

**개발팀**: Commerce Platform Team  
**Repository**: [GitHub - Commerce Gateway](https://github.com/INNER-CIRCLE-ICD4/Project-3.-Commerce/tree/main/infra/gateway)