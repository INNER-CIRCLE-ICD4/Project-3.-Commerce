# Commerce Gateway Service

## 📋 개요

Commerce 프로젝트의 **API Gateway 서비스**입니다. Spring Cloud Gateway를 기반으로 MSA 아키텍처에서 모든 외부 요청의 **단일 진입점(Single Entry Point)** 역할을 수행합니다.

---

## 🎯 주요 기능

### 🔐 인증 및 인가
- **JWT 기반 인증**: OAuth2 Resource Server로 토큰 검증
- **역할 기반 접근 제어**: ROLE_USER, ROLE_ADMIN 권한 관리
- **화이트리스트**: 인증 불필요 경로 설정 (로그인, 회원가입 등)

### 🌐 라우팅 및 프록시
- **동적 라우팅**: 요청 경로에 따른 백엔드 서비스 자동 연결
- **경로 변환**: RewritePath 필터로 서비스별 context-path 처리
- **로드 밸런싱**: 서비스 디스커버리 연동 (Eureka/Cloud Map)

### 🔄 CORS 및 보안
- **Cross-Origin 처리**: 프론트엔드(React/Vue) 요청 허용
- **보안 헤더**: 응답에 보안 관련 헤더 자동 추가
- **사용자 컨텍스트**: 인증된 사용자 정보를 백엔드로 전달

### 📊 분산 추적 및 모니터링
- **Distributed Tracing**: Micrometer + Zipkin으로 요청 추적
- **Trace ID 전파**: 모든 요청에 고유 추적 ID 부여
- **성능 모니터링**: Actuator를 통한 헬스체크 및 메트릭 수집

---

## 🏗️ 아키텍처

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Frontend   │    │   Mobile    │    │   Partner   │
│ (React/Vue) │    │     App     │    │     API     │
└─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │
                  ┌─────────────┐
                  │   Gateway   │ ← YOU ARE HERE
                  │   :8000     │
                  └─────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Member    │    │    Order    │    │   Product   │
│  Service    │    │   Service   │    │   Service   │
│   :8080     │    │    :8081    │    │    :8082    │
└─────────────┘    └─────────────┘    └─────────────┘
```

---

## 🚀 빠른 시작

### 1. 환경 변수 설정
```bash
export JWT_SECRET="your-super-secret-jwt-key-here"
```

### 2. 인프라 서비스 실행
```bash
# Docker Compose로 Zipkin, PostgreSQL 등 실행
docker-compose up -d
```

### 3. Gateway 실행
```bash
# 개발 환경
./gradlew :infra:gateway:bootRun

# 또는 JAR 실행
java -jar build/libs/gateway-1.0.jar
```

### 4. 서비스 확인
```bash
# 헬스체크
curl http://localhost:8000/actuator/health

# 로그인 테스트 (Member Service 경유)
curl -X POST http://localhost:8000/api/member-service/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'
```

---

## 🛠️ 설정

### 라우팅 설정
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
- `GET /api/member-service/members` → `GET http://localhost:8080/api/v1/members`
- `POST /api/member-service/auth/login` → `POST http://localhost:8080/api/v1/auth/login`

### 보안 설정
```yaml
jwt:
  secret: ${JWT_SECRET}
  authority-claim: roles      # JWT에서 권한 정보 클레임명
  principal-claim: email      # JWT에서 사용자 식별 클레임명

cors:
  allowed-origins: "http://localhost:3000,http://localhost:5173"
```

---

## 📝 API 경로

### 인증 관련 (화이트리스트)
- `POST /api/member-service/auth/login` - 로그인
- `POST /api/member-service/auth/refresh` - 토큰 갱신
- `POST /api/member-service/members` - 회원가입

### 보호된 경로 (인증 필요)
- `GET /api/member-service/profile` - 내 프로필 조회
- `PUT /api/member-service/profile` - 프로필 수정
- `GET /api/order-service/**` - 주문 관련 API
- `GET /api/product-service/**` - 상품 관련 API

### 관리자 전용
- `GET /api/member-service/admin/**` - 회원 관리
- `POST /api/product-service/admin/**` - 상품 관리

---

## 🔍 분산 추적

### Zipkin UI 접속
```
http://localhost:9411
```

### 추적 헤더
모든 응답에 추적 정보가 포함됩니다:
```
X-Trace-Id: 550e8400-e29b-41d4-a716-446655440000
X-Span-Id: 446655440000
```

### 로그 패턴
```
[gateway,550e8400e29b41d4a716446655440000,446655440000] INFO  - Processing request
```

---

## 🧪 테스트

### 단위 테스트
```bash
./gradlew :infra:gateway:test
```

### 통합 테스트
```bash
# Gateway + Member Service 연동 테스트
./gradlew :infra:gateway:integrationTest
```

### 수동 테스트
```bash
# 1. 토큰 없이 보호된 경로 접근 (401 예상)
curl http://localhost:8000/api/member-service/profile

# 2. 로그인 후 토큰 획득
TOKEN=$(curl -s -X POST http://localhost:8000/api/member-service/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}' | jq -r '.accessToken')

# 3. 토큰으로 보호된 경로 접근 (200 예상)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/api/member-service/profile
```

---

## 📚 문서

- [라우팅 설정 가이드](docs/GATEWAY_ROUTING_GUIDE.md)
- [분산 추적 가이드](docs/DISTRIBUTED_TRACING_GUIDE.md)
- [구현 계획서](docs/GATEWAY_IMPLEMENTATION_PLAN.md)
- [서비스 디스커버리 비교](docs/service-discovery-comparison.md)

---

## 🛡️ 보안 고려사항

### JWT 설정
- **JWT_SECRET**: 프로덕션에서는 반드시 강력한 시크릿 사용
- **토큰 만료**: AccessToken 15분, RefreshToken 7일 권장
- **알고리즘**: HS256 사용 (RSA256도 지원)

### CORS 설정
- **개발**: `localhost` 도메인 허용
- **프로덕션**: 실제 프론트엔드 도메인으로 제한

### 화이트리스트 관리
- 인증 불필요 경로는 최소한으로 제한
- 정기적인 보안 감사 수행

---

## 🚀 배포

### Docker 빌드
```bash
docker build -t commerce-gateway .
```

### Kubernetes 배포
```bash
kubectl apply -f k8s/gateway-deployment.yaml
```

### 환경별 설정
- **local**: `application-local.yml`
- **prod**: `application-prod.yml`

---

## 📈 모니터링

### 주요 메트릭
- **요청 처리량**: RPS (Requests Per Second)
- **응답 시간**: P95, P99 레이턴시
- **에러율**: 4xx, 5xx 응답 비율
- **서킷 브레이커**: 백엔드 서비스 장애 감지

### 알림 설정
- **높은 에러율**: 5xx > 1%
- **느린 응답**: P95 > 1초
- **서비스 다운**: Health Check 실패

---

## 👥 개발팀

- **Architecture**: MSA 기반 API Gateway
- **Framework**: Spring Cloud Gateway (WebFlux)
- **Security**: Spring Security + OAuth2 Resource Server
- **Monitoring**: Micrometer + Zipkin + Actuator

---

**Port**: 8000  
**Profile**: local, prod  
**Version**: 1.0  
**Last Updated**: 2025-01-11
