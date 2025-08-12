# 분산 추적 (Distributed Tracing) 가이드

## 🎯 분산 추적이란?

### 문제 상황: 분산 추적이 없다면?

MSA 환경에서 사용자가 "상품 주문"을 했는데 실패했다고 가정해보세요:

```
사용자 → Gateway → Member서비스(인증) → Product서비스(재고확인) → Order서비스(주문생성) → Payment서비스(결제처리)
```

**에러 발생 시 각 서비스 로그:**
- Gateway 로그: `2023-11-09 14:30:15 ERROR Order 서비스에서 500 에러`
- Member 로그: `2023-11-09 14:30:16 INFO 사용자 인증 성공`
- Product 로그: `2023-11-09 14:30:17 WARN 재고 부족: 상품ID 456`
- Order 로그: `2023-11-09 14:30:18 ERROR 주문 생성 실패`

🤯 **문제점:**
- 어떤 로그들이 같은 사용자 요청인지 알 수 없음
- 시간으로만 추측해야 함
- 여러 사용자 요청이 동시에 처리되면 더욱 복잡
- 장애 원인 파악에 오랜 시간 소요

### 해결책: 분산 추적

**같은 요청에 고유한 추적 ID를 부여:**

```
[Trace ID: abc123] 사용자 → Gateway → Member → Product → Order → Payment
```

**분산 추적이 적용된 로그:**
- Gateway: `[abc123] 2023-11-09 14:30:15 INFO 사용자 요청 시작`
- Member: `[abc123] 2023-11-09 14:30:16 INFO 사용자 인증 성공 userID=1234`
- Product: `[abc123] 2023-11-09 14:30:17 WARN 재고 부족: 상품ID=456, 요청수량=5, 재고=2`
- Order: `[abc123] 2023-11-09 14:30:18 ERROR 주문 생성 실패: 재고 부족으로 인한 실패`

✅ **장점:**
- 한 눈에 연관된 로그를 찾을 수 있음
- 장애 원인을 빠르게 추적 가능
- 성능 병목 구간 식별 용이

## 📊 분산 추적의 핵심 개념

### 1. Trace (추적)
- **정의**: 하나의 사용자 요청에 대한 전체 여정
- **예시**: "상품 주문 요청 abc123"
- **특징**: 여러 서비스를 거치는 전체 플로우를 추적

### 2. Span (구간)
- **정의**: Trace 내의 각 작업 단위
- **예시**: "Gateway 처리", "Member 인증", "Product 조회"
- **특징**: 시작/종료 시간, 메타데이터 포함

### 3. 시각적 표현

```
Trace ID: abc123 (전체 요청: 150ms)
├── Span: Gateway 처리 (0-100ms)
│   ├── Span: JWT 검증 (5-15ms)
│   └── Span: 라우팅 (20-100ms)
├── Span: Member 인증 (10-30ms)
├── Span: Product 조회 (40-80ms)
│   └── Span: DB 쿼리 (50-75ms)
└── Span: Order 생성 (90-95ms) ❌ 여기서 실패!
```

### 4. Trace Context 전파

```
HTTP Headers를 통한 자동 전파:
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Gateway   │───▶│   Member    │───▶│   Product   │
│             │    │             │    │             │
│ Trace: abc  │    │ Trace: abc  │    │ Trace: abc  │
│ Span:  111  │    │ Span:  222  │    │ Span:  333  │
└─────────────┘    └─────────────┘    └─────────────┘
```

## 🛠️ Spring Cloud Sleuth & Zipkin 선택 이유

### Spring Cloud Sleuth

#### 역할
- 자동으로 Trace ID/Span ID를 생성하고 전파
- 기존 애플리케이션 코드 변경 없이 분산 추적 적용

#### 장점

1. **Spring Boot 자동 설정**
   ```kotlin
   // build.gradle.kts에 의존성만 추가
   implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
   // 별도 설정 없이 바로 동작!
   ```

2. **자동 계측 (Auto-Instrumentation)**
   - HTTP 요청/응답
   - 데이터베이스 쿼리 (JDBC, JPA)
   - 메시지 큐 (RabbitMQ, Kafka)
   - 외부 API 호출 (RestTemplate, WebClient)

3. **로그 통합**
   ```java
   // 기존 로그
   logger.info("상품 조회 완료: productId={}", productId);
   
   // Sleuth 적용 후 (코드 변경 없음)
   [abc123,def456] INFO 상품 조회 완료: productId=123
   //  ↑      ↑
   // Trace  Span
   ```

4. **표준 준수**
   - OpenTracing 표준 지원
   - OpenTelemetry 마이그레이션 경로 제공

#### 자동으로 생성되는 Span들

```java
@RestController
public class ProductController {
    
    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        // Sleuth가 자동으로 생성하는 Span들:
        // 1. HTTP Server Span: GET /products/123
        // 2. Method Execution Span: ProductController.getProduct
        
        Product product = productService.findById(id);
        // 3. Database Span: SELECT * FROM products WHERE id = ?
        
        return product;
    }
}
```

### Zipkin

#### 역할
- 분산 추적 데이터를 수집, 저장, 시각화

#### 장점

1. **직관적인 UI**
   ```
   Zipkin UI Timeline View:
   
   abc123 |████Gateway█████|
          |  ██Member██    |
          |    ████Product████|
          |      ██Order██   | ❌ Error
          0ms   50ms   100ms 150ms
   ```

2. **성능 분석**
   - 전체 요청 시간 분석
   - 병목 구간 식별
   - 서비스별 응답 시간 비교

3. **에러 추적**
   - 실패한 요청의 정확한 지점 확인
   - 에러 발생 서비스와 원인 파악
   - 스택 트레이스와 연동

4. **간편한 설치**
   ```bash
   # Docker로 1분 만에 실행
   docker run -d -p 9411:9411 openzipkin/zipkin
   ```

## 🔄 실제 동작 흐름

### 1. Sleuth의 자동 동작

```java
// 개발자가 작성한 원본 코드
@GetMapping("/products/{id}")
public Product getProduct(@PathVariable Long id) {
    return productService.findById(id);
}

// Sleuth가 Runtime에 자동으로 추가하는 것들:
// 1. 요청 시작 시 Span 생성 (또는 기존 Trace에서 새 Span)
// 2. HTTP 헤더에서 Trace Context 추출/생성
// 3. 다음 서비스 호출 시 HTTP 헤더에 Trace 정보 전파
// 4. 로그에 [trace-id,span-id] 자동 추가
// 5. 요청 완료 시 Zipkin에 Span 데이터 비동기 전송
```

### 2. 서비스 간 자동 전파

```
1. 클라이언트 요청
   ↓
2. Gateway [trace:abc123, span:111]
   - 새로운 Trace 생성
   - HTTP 헤더에 trace 정보 추가
   ↓
3. Member Service [trace:abc123, span:222]
   - 헤더에서 trace 정보 추출
   - 새로운 Span 생성 (같은 Trace 내)
   ↓
4. Product Service [trace:abc123, span:333]
   - Trace 계속 이어짐
   - DB 쿼리도 자동으로 Child Span 생성
   ↓
5. 모든 Span 데이터 Zipkin에 전송
```

### 3. Zipkin UI에서의 표현

#### 서비스 맵 뷰
```
[Gateway] ───→ [Member] ───→ [Product] ───→ [Order]
    │              │            │           │
   120ms          45ms         80ms       ❌ 5ms
```

#### 타임라인 뷰
```
Trace abc123 (Total: 250ms)
│
├─ gateway        ████████████████████████████████ 200ms
│  ├─ jwt-verify  ██ 10ms
│  └─ routing     ████████████████████████████ 190ms
│
├─ member         ████████ 45ms (concurrent)
│  └─ db-query    ████ 20ms
│
├─ product        ████████████ 80ms
│  ├─ cache-get   ██ 10ms
│  └─ db-query    ████████ 60ms
│
└─ order          ██ 5ms ❌ ERROR
   └─ validation  ██ 5ms (failed)
```

## 💡 다른 도구들과의 비교

### vs OpenTelemetry
| 항목 | Sleuth | OpenTelemetry |
|------|--------|---------------|
| **학습 비용** | 낮음 (Spring 자동설정) | 높음 (수동 설정) |
| **표준 준수** | OpenTracing 기반 | 최신 표준 |
| **생태계** | Spring 중심 | 벤더 중립적 |
| **성숙도** | 안정적 | 빠르게 발전 중 |

### vs Jaeger
| 항목 | Zipkin | Jaeger |
|------|--------|--------|
| **설치 복잡도** | 낮음 | 중간 |
| **성능** | 좋음 | 매우 좋음 |
| **확장성** | 중간 | 높음 |
| **Spring 통합** | 완벽 | 좋음 |

### vs 클라우드 서비스 (AWS X-Ray, Google Cloud Trace)
| 항목 | Sleuth+Zipkin | 클라우드 서비스 |
|------|---------------|-----------------|
| **비용** | 무료 | 사용량 기반 과금 |
| **벤더 종속** | 없음 | 높음 |
| **클라우드 통합** | 수동 설정 | 자동 연동 |
| **온프레미스** | 가능 | 제한적 |

## 🚀 구현 단계별 가이드

### Phase 1: 상관관계 ID만 (즉시 적용 가능)

```java
// 게이트웨이에 CorrelationIdFilter 추가
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = extractOrGenerateCorrelationId(exchange.getRequest());
        
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
```

**각 서비스에서 로깅 시:**
```java
// MDC를 이용한 로그 컨텍스트 설정
@Component
public class CorrelationInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.clear();
    }
}

// logback-spring.xml 패턴
<pattern>[%X{correlationId:-}] %d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
```

### Phase 2: Spring Cloud Sleuth 도입

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    // 옵션: Zipkin 연동
    implementation("org.springframework.cloud:spring-cloud-sleuth-zipkin")
}
```

```yaml
# application.yml
spring:
  sleuth:
    sampler:
      probability: 1.0  # 개발: 100% 샘플링, 운영: 0.1 (10%)
    zipkin:
      base-url: http://localhost:9411  # Zipkin 서버 주소
  application:
    name: gateway  # 서비스 이름 (Zipkin에서 구분용)
```

### Phase 3: Zipkin 서버 구축

```bash
# Docker로 Zipkin 실행
docker run -d \
  --name zipkin \
  -p 9411:9411 \
  openzipkin/zipkin

# 브라우저에서 접속
open http://localhost:9411
```

### Phase 4: 고급 설정

```yaml
# 운영환경 최적화
spring:
  sleuth:
    sampler:
      probability: 0.1  # 10%만 샘플링 (성능 고려)
    zipkin:
      sender:
        type: kafka  # 고성능을 위한 Kafka 사용
    span:
      skip-pattern: "^/actuator.*"  # 헬스체크는 추적 제외
```

## 📈 성능 고려사항

### 1. 샘플링 비율 조정
```yaml
spring:
  sleuth:
    sampler:
      probability: 0.1  # 10% 샘플링
      # 또는 rate-limiting 방식
      rate: 100  # 초당 100개 요청만 추적
```

### 2. 비동기 전송
- Zipkin으로의 데이터 전송은 별도 스레드에서 비동기 처리
- 메인 요청 처리에는 영향 없음

### 3. 메모리 오버헤드
- Span 데이터는 메모리에 임시 저장 후 일괄 전송
- 대용량 트래픽 시 메모리 모니터링 필요

## 🎯 MSA에서의 실제 효과

### Before (분산 추적 없음)
```
🚨 장애 발생!
👨‍💻 개발자: "어디서 에러가 났지?"
⏰ 30분 후: "게이트웨이 로그를 보니..."
⏰ 1시간 후: "멤버 서비스도 확인해보자..."
⏰ 2시간 후: "아, 상품 서비스에서 DB 타임아웃이었구나!"
```

### After (분산 추적 적용)
```
🚨 장애 발생!
👨‍💻 개발자: Zipkin에서 에러 Trace 검색
⏰ 5분 후: "Trace abc123을 보니 Product 서비스의 DB 쿼리에서 5초 지연!"
⏰ 10분 후: 문제 해결 완료
```

### 실제 개발 시나리오

1. **성능 병목 발견**
   - "전체 응답이 느린데 어느 구간이 문제일까?"
   - Zipkin에서 한눈에 파악: "Order 서비스의 결제 API 호출이 3초"

2. **연쇄 장애 추적**
   - "A 서비스 장애가 B, C 서비스에 어떤 영향을 주고 있을까?"
   - Trace로 전체 호출 체인과 실패 지점 확인

3. **신기능 테스트**
   - "새로운 추천 서비스를 추가했는데 전체 플로우가 어떻게 변했을까?"
   - 서비스 의존성 맵으로 시각적 확인

## 🔗 다음 단계

### 1. 메트릭과 연동
```yaml
# Micrometer + Prometheus 연동
management:
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 1.0
```

### 2. 로그 집중화
```yaml
# ELK Stack 연동
logging:
  pattern:
    console: "[%X{traceId:-},%X{spanId:-}] %d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 3. 알림 연동
```java
// 에러 Span 발생 시 Slack 알림
@EventListener
public void handleSpanFinished(FinishedSpanEvent event) {
    if (event.getSpan().getTags().containsKey("error")) {
        slackNotificationService.sendAlert(event.getSpan());
    }
}
```

## 📚 참고 자료

- [Spring Cloud Sleuth 공식 문서](https://spring.io/projects/spring-cloud-sleuth)
- [Zipkin 공식 사이트](https://zipkin.io/)
- [OpenTracing 표준](https://opentracing.io/)
- [분산 추적 베스트 프랙티스](https://microservices.io/patterns/observability/distributed-tracing.html)

---

분산 추적은 MSA 개발에서 **필수**가 된 기술입니다. 특히 여러 팀이 다른 서비스를 개발하는 환경에서는 디버깅과 성능 최적화에 결정적인 역할을 합니다. 

**즉시 시작할 수 있는 Phase 1부터 단계별로 적용해보세요!** 🚀
