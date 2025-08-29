# 🚀 Gateway 메모리 기반 Rate Limiting 구현 가이드

## 📋 개요

Gateway에 **토큰 버킷 알고리즘**을 사용한 **메모리 기반 Rate Limiting 시스템**을 구현하여 API 요청 횟수를 제한하고 DDoS 공격을 방어합니다.

### 🎯 목표
- **백엔드 보호**: Member, Order, Product 서비스의 과부하 방지
- **보안 강화**: 브루트포스, DDoS 공격 차단
- **성능 최적화**: Redis 없이 메모리만으로 빠른 처리

---

## 🪣 토큰 버킷 알고리즘

### 📊 동작 원리
```
🪣 Token Bucket
├── 용량(Capacity): 최대 토큰 수 (버스트 허용량)
├── 보충률(Refill Rate): 초당 추가되는 토큰 수
├── 소비(Consume): 요청당 사용하는 토큰 수
└── 제한(Limit): 토큰 부족 시 요청 거부

⏰ 시간별 예시 (용량: 10, 보충률: 초당 2개):
0초: [🪙🪙🪙🪙🪙🪙🪙🪙🪙🪙] (10개 - 가득)
1초: [🪙🪙🪙🪙] (8개 - 6번 요청 처리)
2초: [🪙🪙🪙🪙🪙🪙] (6개 - 2개 보충)
3초: [🪙🪙🪙🪙🪙] (5개 - 3번 요청 + 2개 보충)
```

### ⚡ 장점
- ✅ **버스트 트래픽 허용**: 일시적 높은 요청 처리 가능
- ✅ **평균 제한 유지**: 장기적으로 설정 비율 준수
- ✅ **메모리 효율**: Redis 불필요, 간단한 구조
- ✅ **직관적 이해**: 디버깅과 튜닝이 쉬움

---

## 🔧 구현 아키텍처

### 📁 컴포넌트 구조
```
📁 gateway/
├── ratelimit/
│   ├── TokenBucket.java          # 토큰 버킷 알고리즘 구현
│   ├── RateLimitService.java     # 메인 Rate Limiting 서비스
│   ├── RateLimitConfig.java      # 설정 및 정책 클래스
│   └── RateLimitResult.java      # 결과 응답 클래스
├── filter/
│   └── RateLimitingFilter.java   # Global Filter 구현
└── controller/
    └── RateLimitController.java  # 모니터링 및 관리 API
```

### 🔄 플로우 다이어그램
```
Client Request
      ↓
RateLimitingFilter (Order: -50)
      ↓
Key Generation (IP/User)
      ↓
TokenBucket.tryConsume()
      ↓
   Allowed?
  ↙        ↘
✅ Yes      ❌ No
  ↓          ↓
Continue   429 Response
  ↓          ↓
UserContextFilter  Retry-After Header
  ↓
Backend Service
```

---

## 📊 제한 정책 설계

### 🎯 경로별 차등 제한
| API 유형 | 경로 패턴 | 용량 | 보충률(초당) | 목적 |
|----------|-----------|------|-------------|------|
| **로그인 API** | `/auth/**` | 10개 | 2개 | 브루트포스 방어 |
| **관리자 API** | `/admin/**` | 30개 | 10개 | 중요 API 보호 |
| **일반 API** | `/api/**` | 100개 | 50개 | 사용자 편의 |
| **헬스체크** | `/actuator/health` | 200개 | 100개 | 모니터링 허용 |

### 🔑 키 생성 전략
```java
// 인증된 사용자: 사용자별 제한
key = "user:" + userId

// 미인증 요청: IP별 제한  
key = "ip:" + clientIP

// 예시:
"user:123456789"     // JWT 토큰의 subject
"ip:192.168.1.100"   // X-Forwarded-For 헤더
```

---

## 🛠️ 핵심 구현 클래스

### 1. 🪣 TokenBucket.java
```java
public class TokenBucket {
    private final int capacity;           // 최대 용량
    private final int refillRate;         // 초당 보충률
    private final AtomicInteger tokens;   // 현재 토큰 수
    private volatile long lastRefillTime; // 마지막 보충 시간
    
    // 핵심 메서드
    public synchronized void refill()               // 시간 기반 토큰 보충
    public boolean tryConsume(int requestedTokens)  // 토큰 소비 시도
    public long getSecondsUntilRefill()            // 다음 보충까지 시간
}
```

### 2. 🎯 RateLimitService.java
```java
@Service
public class RateLimitService {
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // 핵심 메서드
    public RateLimitResult checkLimit(String key, RateLimitConfig config)
    private void refillAllBuckets()      // 5초마다 실행
    private void cleanupOldBuckets()     // 30분마다 실행
    public Map<String, Object> getStats() // 모니터링 정보
}
```

### 3. 🌐 RateLimitingFilter.java
```java
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 경로별 설정 결정
        RateLimitConfig config = getRateLimitConfig(path);
        
        // 2. 키 생성 (IP 또는 사용자)
        return generateRateLimitKey(exchange)
            .flatMap(key -> {
                // 3. Rate Limit 체크
                RateLimitResult result = rateLimitService.checkLimit(key, config);
                
                if (result.isAllowed()) {
                    // 4a. 허용 - 헤더 추가 후 진행
                    addRateLimitHeaders(exchange, result);
                    return chain.filter(exchange);
                } else {
                    // 4b. 차단 - 429 응답
                    return createRateLimitExceededResponse(exchange, result);
                }
            });
    }
    
    @Override
    public int getOrder() {
        return -50; // UserContextFilter보다 먼저 실행
    }
}
```

---

## 📈 HTTP 응답 설계

### ✅ 정상 요청 (허용)
```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 7
Content-Type: application/json

{
  "data": "정상 응답 데이터"
}
```

### 🚫 제한 초과 (차단)
```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 0
Retry-After: 30
Content-Type: application/json

{
  "success": false,
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "요청 횟수가 제한을 초과했습니다",
  "limit": 10,
  "retryAfter": 30,
  "timestamp": 1640995200000
}
```

---

## 📊 모니터링 및 관리

### 🔍 상태 조회 API
```bash
# 전체 Rate Limit 상태
GET /actuator/ratelimit/stats

# 응답 예시
{
  "totalBuckets": 45,
  "totalBlocked": 123,
  "lastCleanupTime": "2025-01-18T10:30:00",
  "bucketDetails": {
    "ip:192.168.1.100": {
      "tokens": 8,
      "capacity": 10,
      "lastRefill": "2025-01-18T10:29:55"
    }
  }
}
```

### 🔄 관리 API
```bash
# 특정 키 Rate Limit 리셋
DELETE /actuator/ratelimit/reset/ip:192.168.1.100

# 모든 Rate Limit 리셋  
DELETE /actuator/ratelimit/reset-all

# 응답
{
  "message": "Rate limit reset successfully",
  "key": "ip:192.168.1.100"
}
```

---

## 🚀 배포 및 운영

### ⚙️ 메모리 관리
- **자동 정리**: 1시간 미사용 버킷 제거
- **주기적 보충**: 5초마다 토큰 보충
- **메모리 효율**: ConcurrentHashMap + AtomicInteger 사용

### 📈 성능 최적화
- **빠른 응답**: O(1) 시간복잡도
- **메모리 절약**: Redis 서버 불필요
- **동시성**: CAS 연산으로 Thread-Safe

### 🔧 튜닝 가이드
```java
// 브루트포스 공격이 심한 경우
TokenBucket authBucket = new TokenBucket(5, 1);  // 더 엄격하게

// 사용자가 많은 경우
TokenBucket generalBucket = new TokenBucket(200, 100);  // 더 관대하게

// API 응답이 느린 경우
TokenBucket adminBucket = new TokenBucket(10, 5);  // 보수적으로
```

---

## 🧪 테스트 시나리오

### 1. 📝 단위 테스트
```java
@Test
void tokenBucket_정상_소비_테스트() {
    TokenBucket bucket = new TokenBucket(10, 5);
    
    // 10번 연속 요청 - 모두 성공
    for (int i = 0; i < 10; i++) {
        assertTrue(bucket.tryConsume(1));
    }
    
    // 11번째 요청 - 실패
    assertFalse(bucket.tryConsume(1));
}

@Test
void tokenBucket_보충_테스트() throws InterruptedException {
    TokenBucket bucket = new TokenBucket(10, 5);
    
    // 토큰 모두 소비
    for (int i = 0; i < 10; i++) {
        bucket.tryConsume(1);
    }
    
    // 1초 대기 후 토큰 보충 확인
    Thread.sleep(1000);
    assertTrue(bucket.tryConsume(1)); // 5개 보충되어 성공
}
```

### 2. 🌐 통합 테스트
```bash
# 정상 요청 테스트
for i in {1..5}; do
  curl -X POST http://localhost:8000/api/member-service/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"password123"}'
done

# Rate Limit 초과 테스트
for i in {6..15}; do
  curl -X POST http://localhost:8000/api/member-service/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"password123"}'
  # 예상: 6번째부터 429 응답
done
```

### 3. 📊 부하 테스트
```bash
# Apache Bench로 동시 요청 테스트
ab -n 100 -c 10 http://localhost:8000/api/member-service/auth/login

# 예상 결과:
# - 처음 10개: 즉시 처리 (버킷 용량)
# - 나머지 90개: 보충률에 따라 처리
```

---

## 🛡️ 보안 고려사항

### 🎯 공격 시나리오 대응
- **Distributed Attack**: IP별 제한으로 분산 공격 완화
- **Authenticated Attack**: 사용자별 제한으로 계정 탈취 공격 방어  
- **API Abuse**: 경로별 차등 제한으로 중요 API 보호

### 🔒 추가 보안 기능
- **JWT 블랙리스트**: 기존 토큰 무효화와 연계
- **브루트포스 방어**: Member Service LoginAttemptService와 협력
- **모니터링**: 비정상 패턴 감지 및 알림

---

## 📚 참고 자료

### 🔗 관련 문서
- [JWT Authentication Guide](./JWT_AUTHENTICATION_GUIDE.md)
- [Gateway Routing Guide](./GATEWAY_ROUTING_GUIDE.md)
- [Distributed Tracing Guide](./DISTRIBUTED_TRACING_GUIDE.md)

### 📖 외부 참고
- [Token Bucket Algorithm - Wikipedia](https://en.wikipedia.org/wiki/Token_bucket)
- [AWS API Gateway Throttling](https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-request-throttling.html)
- [Spring Cloud Gateway Rate Limiting](https://spring.io/blog/2018/04/23/spring-cloud-gateway-rate-limiting)

---

**📅 작성일**: 2025-08-29 
**✍️ 작성자**: Commerce Platform Team  
**🔄 버전**: 1.0  
**📌 상태**: 구현 예정
