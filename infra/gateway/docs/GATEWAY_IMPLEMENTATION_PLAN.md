# Gateway 구현 계획

## 🎯 목표
Spring Cloud Gateway를 기반으로 MSA 환경에서 인증/인가 처리와 라우팅을 담당하는 API Gateway 구현

## 📋 현재 상태
- ✅ Spring Cloud Gateway WebFlux 기본 설정 완료
- ✅ Security, OAuth2 Resource Server 의존성 추가 완료
- ✅ 기본 라우팅 설정 (member-service, review-service)
- ⚠️ JWT, CORS 설정값에 오타 있음 (수정 필요)

## 🚀 구현 계획

### Phase 1: 핵심 인증/인가 (우선순위: 높음)

#### 1.1 JWT 토큰 검증 설정
- [ ] `ReactiveJwtDecoder` 빈 구성
- [ ] JWT 서명 키 설정 (회원 서비스와 동일한 키 사용)
- [ ] 클레임 추출 설정 (`memberId`, `roles`)

**파일 위치:**
- `src/main/java/commerce/gateway/security/SecurityConfig.java`

#### 1.2 보안 필터 체인 구성
- [ ] 화이트리스트 경로 설정 (`/api/member-service/auth/**`, `/actuator/**`)
- [ ] 인증이 필요한 경로 설정
- [ ] 경로별 권한 설정 (필요시)

**화이트리스트 경로:**
```
/api/member-service/auth/**  # 로그인, 회원가입, 토큰 갱신
/actuator/**                 # 헬스체크, 메트릭
/swagger-ui/**              # API 문서
/v3/api-docs/**             # OpenAPI 스펙
```

#### 1.3 인증/인가 실패 시 공통 응답 처리
- [ ] 401 Unauthorized 응답 커스터마이징
- [ ] 403 Forbidden 응답 커스터마이징
- [ ] JSON 형태의 일관된 에러 응답

**응답 포맷:**
```json
{
  "success": false,
  "code": "AUTH-401",
  "message": "Unauthorized",
  "traceId": "correlation-id",
  "timestamp": 1731135794123
}
```

### Phase 2: 사용자 컨텍스트 전파 (우선순위: 높음)

#### 2.1 UserContextFilter 구현
- [ ] 인증 성공 시 JWT에서 사용자 정보 추출
- [ ] `X-Member-Id`, `X-Roles` 헤더로 다운스트림 서비스에 전달
- [ ] `GlobalFilter` 구현 및 적절한 Order 설정

**파일 위치:**
- `src/main/java/commerce/gateway/security/UserContextFilter.java`

### Phase 3: CORS 및 기본 설정 (우선순위: 중간)

#### 3.1 CORS 설정 수정
- [ ] 현재 오타 수정 (`allowd-origins` → `allowed-origins`)
- [ ] 환경별 허용 오리진 설정
- [ ] Credentials 허용 설정

#### 3.2 상관관계 ID 관리
- [ ] `X-Correlation-Id` 생성/전파 필터
- [ ] 요청 추적을 위한 로깅

**파일 위치:**
- `src/main/java/commerce/gateway/filter/CorrelationIdFilter.java`

### Phase 4: 라우팅 확장 (우선순위: 중간)

#### 4.1 추가 서비스 라우팅
- [ ] Product 서비스 라우팅 추가
- [ ] Order 서비스 라우팅 추가
- [ ] Search 서비스 라우팅 추가

#### 4.2 라우팅 최적화
- [ ] 로드밸런싱 설정 (`lb://service-name`)
- [ ] 기본 타임아웃 설정
- [ ] 재시도 정책 설정 (선택사항)

### Phase 5: 예외 처리 및 관찰가능성 (우선순위: 낮음)

#### 5.1 글로벌 예외 처리
- [ ] `ErrorWebExceptionHandler` 구현
- [ ] 게이트웨이 레벨 오류 표준화 (502, 504 등)
- [ ] 서비스 오류는 pass-through

**파일 위치:**
- `src/main/java/commerce/gateway/exception/GlobalErrorHandler.java`

#### 5.2 로깅 및 모니터링
- [ ] 구조적 로깅 설정
- [ ] 요청/응답 로깅 필터 (선택사항)
- [ ] 메트릭 수집 설정

### Phase 6: 운영 고도화 (우선순위: 낮음)

#### 6.1 성능 최적화
- [ ] 레이트 리미팅 (Redis 기반)
- [ ] 서킷 브레이커 패턴
- [ ] 캐싱 전략

#### 6.2 보안 강화
- [ ] IP 허용/차단 필터
- [ ] 요청 크기 제한
- [ ] 헤더 검증

## 🔧 설정 파일 수정 사항

### application.yml 수정 필요
```yaml
# 현재 오타 수정 필요
jwt:
  authority-claim: roles
  principal-claim: memberId  # email → memberId 변경 고려
cors:
  allowed-origins: "http://localhost:3000,http://localhost:5173"  # 오타 수정
```

### 환경별 설정 분리
- **local**: 로컬 개발용 설정
- **prod**: 운영 환경용 설정 (환경변수 활용)

## 📁 파일 구조

```
src/main/java/commerce/gateway/
├── GatewayApplication.java
├── security/
│   ├── SecurityConfig.java           # JWT 검증, 보안 설정
│   └── UserContextFilter.java       # 사용자 컨텍스트 전파
├── filter/
│   └── CorrelationIdFilter.java     # 상관관계 ID 관리
├── exception/
│   └── GlobalErrorHandler.java      # 글로벌 예외 처리
└── config/
    └── CorsConfig.java              # CORS 설정 (필요시)
```

## 🧪 테스트 계획

### 단위 테스트
- [ ] SecurityConfig 테스트
- [ ] UserContextFilter 테스트
- [ ] JWT 검증 로직 테스트

### 통합 테스트
- [ ] 인증/인가 플로우 테스트
- [ ] 라우팅 테스트
- [ ] 에러 응답 테스트

### 수동 테스트 시나리오
1. **인증 없는 요청**: 401 JSON 응답 확인
2. **권한 부족 요청**: 403 JSON 응답 확인
3. **정상 인증 요청**: 헤더 전파 확인
4. **CORS 요청**: Preflight 처리 확인
5. **화이트리스트 요청**: 인증 없이 통과 확인

## 📝 체크리스트

### 필수 구현
- [ ] JWT 토큰 검증 설정
- [ ] 보안 필터 체인 구성
- [ ] 401/403 에러 응답 커스터마이징
- [ ] 사용자 컨텍스트 헤더 전파
- [ ] CORS 설정 수정
- [ ] 기본 라우팅 완성

### 선택 구현
- [ ] 상관관계 ID 관리
- [ ] 글로벌 예외 처리
- [ ] 성능 최적화 (레이트 리미팅, 서킷 브레이커)
- [ ] 보안 강화 (IP 필터, 요청 제한)

## 🚨 주의사항

1. **JWT 서명 키 동기화**: 회원 서비스와 게이트웨이가 동일한 키 사용 필수
2. **클레임 스키마 통일**: `memberId`, `roles` 필드명 사전 협의
3. **환경별 설정**: 운영 환경에서는 반드시 환경변수로 민감 정보 주입
4. **에러 응답 표준화**: 서비스 간 일관된 에러 포맷 유지
5. **성능 고려**: 게이트웨이는 모든 요청의 단일 진입점이므로 성능 최적화 중요

## 📚 참고 자료

- [Spring Cloud Gateway 공식 문서](https://spring.io/projects/spring-cloud-gateway)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-jwt-bcp)
