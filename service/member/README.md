# 👥 Member Service

Commerce MSA 플랫폼의 **회원 관리 및 인증** 마이크로서비스입니다.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen?logo=spring-boot)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![JWT](https://img.shields.io/badge/JWT-0.12.3-purple)

---

## 📋 목차

- [개요](#-개요)
- [아키텍처](#-아키텍처)
- [주요 기능](#-주요-기능)
  - [회원 관리](#1-회원-관리)
  - [회원 검색](#2-회원-검색-🔍)
  - [인증 시스템](#3-인증-시스템)
    - [로그인 보안 (브루트포스 방어)](#34-로그인-보안-브루트포스-방어-️)
- [API 가이드](#-api-가이드)
- [개발 환경 설정](#-개발-환경-설정)
- [데이터베이스](#-데이터베이스)
- [보안](#-보안)
- [테스트](#-테스트)
- [모니터링 & 트레이싱](#-모니터링--트레이싱)
- [문서](#-관련-문서)

---

## 🎯 개요

### 책임과 역할

**Member Service**는 Commerce 플랫폼에서 다음과 같은 핵심 책임을 담당합니다:

- 👤 **회원 관리**: 사용자 등록, 프로필 관리, 상태 관리
- 🔐 **인증**: JWT 기반 로그인/로그아웃, 토큰 관리
- 🛡️ **권한 관리**: 역할 기반 접근 제어 (RBAC)
- 🔒 **보안**: 비밀번호 암호화, 토큰 검증

### 기술 스택

| 영역 | 기술 | 버전 | 용도 |
|------|------|------|------|
| **Framework** | Spring Boot | 3.5.3 | 애플리케이션 프레임워크 |
| **Architecture** | Hexagonal + CQRS | - | 아키텍처 패턴 |
| **Security** | Spring Security + JWT | 6.x + 0.12.3 | 인증/인가 |
| **Database** | PostgreSQL | 16 | 운영 데이터베이스 |
| **ORM** | Spring Data JPA | 3.x | 데이터 접근 계층 |
| **Search** | JPQL + Native SQL | - | 복합 검색 쿼리 |
| **Password** | BCrypt | 0.10.2 | 비밀번호 암호화 |
| **Tracing** | Micrometer Tracing + Zipkin | 1.x | 분산 추적 |
| **ID Generation** | Snowflake | Custom | 분산 ID 생성 |
| **Config** | Spring Cloud Config | 4.x | 외부 설정 관리 |

---

## 🏗️ 아키텍처

### 헥사고날 아키텍처 + CQRS 패턴

```
┌─────────────────────────────────────────────────────────────┐
│                    Member Service                           │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure Layer (Adapters)                            │
│  ├── in/  : AuthController, MemberController                │
│  ├── out/ : JpaRepository, JwtTokenAdapter, BCryptAdapter   │
│  └── persistence/ : MemberJpaRepository                     │
├─────────────────────────────────────────────────────────────┤
│  Application Layer (CQRS Use Cases)                         │
│  ├── service/ : MemberApplicationService                    │
│  ├── port/in/  : MemberUseCase (Command + Query)            │
│  ├── port/out/ : MemberCommandPort, MemberQueryPort         │
│  └── dto/ : Request/Response DTOs                           │
├─────────────────────────────────────────────────────────────┤
│  Domain Layer (Business Logic)                              │
│  ├── member/ : Member, Email, MemberRole, MemberStatus      │
│  ├── auth/   : JwtTokenInfo, LoginRequest, UserAuthInfo     │
│  └── validation/ : 도메인 검증 규칙                             │ 
└─────────────────────────────────────────────────────────────┘
```

**CQRS (Command Query Responsibility Segregation)**:
- **Command Side**: 회원 생성, 수정, 삭제 등 상태 변경
- **Query Side**: 회원 검색, 조회 등 데이터 읽기
- **분리된 포트**: `MemberCommandPort`, `MemberQueryPort`

### MSA 내에서의 위치

```mermaid
graph TD
    Client[Client] --> Gateway[Gateway]
    Gateway --> Member[Member Service]
    Gateway --> Order[Order Service]
    Gateway --> Product[Product Service]
    
    Member --> DB1[(PostgreSQL)]
    Order --> DB2[(Order DB)]
    Product --> DB3[(Product DB)]
    
    Member -.-> ConfigServer[Config Server]
```

---

## 🔧 주요 기능

### 1. 회원 관리

#### 1.1 회원 가입 📝

**기능**: 새로운 사용자 등록 및 기본 역할 할당

**비즈니스 규칙**:
- 이메일 중복 불가
- 비밀번호 BCrypt 암호화
- 기본적으로 `BUYER` 역할 할당
- Snowflake ID로 고유 식별자 생성

**구현 예시**:
```java
@PostMapping("/members")
public ResponseEntity<MemberResponse> createMember(@RequestBody MemberCreateRequest request) {
    MemberResponse member = memberUseCase.createMember(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(member);
}
```

**도메인 로직**:
```java
public static Member create(String email, String name, String password, String birthDate, String gender) {
    Member member = new Member();
    member.email = new Email(email);           // 이메일 유효성 검증
    member.setName(name);                      // 이름 검증
    member.setPassword(password);              // 비밀번호 정책 검증
    member.status = MemberStatus.ACTIVE;       // 기본 활성화
    member.assignBuyerRole();                  // BUYER 역할 할당
    return member;
}
```

#### 1.2 회원 조회 👀

**기능**: 회원 정보 조회 (본인/관리자만 가능)

**권한 제어**:
```java
@GetMapping("/{memberId}")
public ResponseEntity<MemberResponse> getMember(
        @PathVariable Long memberId,
        @CurrentUser AuthenticatedUser currentUser) {
    
    // 권한 체크: 본인 또는 관리자만
    if (!currentUser.canAccess(memberId)) {
        throw new ForbiddenException("권한이 없습니다.");
    }
    
    return ResponseEntity.ok(memberUseCase.getMember(memberId));
}
```

#### 1.3 프로필 관리 ✏️

**기능**: 회원 정보 수정, 상태 변경

**지원하는 정보**:
- 기본 정보: 이름, 생년월일, 성별
- 상태 관리: ACTIVE, INACTIVE, SUSPENDED
- 역할 관리: BUYER, SELLER, ADMIN

### 2. 회원 검색 🔍

#### 2.1 복합 검색 기능

**기능**: 다양한 조건을 통한 회원 검색 및 페이징 처리

**지원하는 검색 조건**:
```java
public record MemberSearchRequest(
    String keyword,          // 통합 검색 (이름, 이메일)
    String name,            // 이름 검색
    String email,           // 이메일 검색  
    MemberStatus memberStatus, // 회원 상태
    RoleType role,          // 권한 역할
    int page,               // 페이지 번호
    int size                // 페이지 크기
) {}
```

**검색 특징**:
- **🔍 키워드 검색**: 이름과 이메일을 동시에 검색
- **🎯 정확한 필터링**: 상태, 역할별 필터링
- **📄 페이징 처리**: 대용량 데이터 효율적 처리
- **🚀 성능 최적화**: Query Cache + 배치 처리
- **🔗 관계 데이터**: 역할 정보 포함 조회

#### 2.2 검색 쿼리 아키텍처

**쿼리 분리 전략** (대용량 처리 최적화):
```java
// 1단계: 조건에 맞는 Member만 조회 (페이징 적용)
@Query(value = """
    SELECT DISTINCT m FROM Member m
    LEFT JOIN m.roles mr 
    WHERE (:keyword IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (:status IS NULL OR m.status = :status) 
    AND (:role IS NULL OR EXISTS (
        SELECT 1 FROM MemberRole subMr 
        WHERE subMr.member = m AND subMr.roleType = :role
    ))
    ORDER BY m.createAt DESC
    """)
Page<Member> searchMembers(...);

// 2단계: 해당 Member들의 역할 정보를 배치로 조회
@Query("SELECT m FROM Member m JOIN FETCH m.roles mr WHERE m.id IN :memberIds")
List<Member> findMembersWithRoles(@Param("memberIds") List<Long> memberIds);
```

**성능 최적화**:
- **메모리 페이징 방지**: 쿼리 분리로 DB 레벨 페이징
- **N+1 문제 해결**: 배치 조회로 역할 정보 로딩
- **캐시 활용**: Query Cache + 배치 크기 최적화

#### 2.3 권한 기반 접근 제어

**관리자만 접근 가능**:
```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<MemberPageResponse<MemberSearchResponse>> searchMembers(
    @ModelAttribute MemberSearchRequest request
) {
    // 관리자만 회원 검색 가능
}
```

#### 2.4 PostgreSQL 호환성

**Native Query 지원** (JPQL 호환성 문제 해결):
```sql
-- PostgreSQL 전용 최적화
SELECT DISTINCT m.* FROM member m
LEFT JOIN member_role mr ON m.id = mr.member_id
WHERE (:keyword IS NULL OR (
       LOWER(m.name) ILIKE '%' || :keyword || '%' OR 
       LOWER(m.email) ILIKE '%' || :keyword || '%'))
AND (:status IS NULL OR m.status::text = :status)
ORDER BY m.create_at DESC
```

### 3. 인증 시스템

#### 3.1 JWT 기반 로그인 🔐

**기능**: 이메일/비밀번호 인증 후 JWT 토큰 발급

**토큰 구조**:
```json
{
  "sub": "2158078162337996800",    // 사용자 ID (Snowflake)
  "email": "user@example.com",     // 이메일
  "roles": "BUYER,SELLER",         // 역할 (쉼표 구분)
  "type": "ACCESS",                // 토큰 타입
  "iat": 1705520430,               // 발급 시간
  "exp": 1705524030                // 만료 시간 (1시간)
}
```

**로그인 플로우**:
```java
@PostMapping("/auth/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    // 1. 사용자 인증
    UserAuthInfo userAuthInfo = userAuthInfoProvider.findByEmail(request.email());
    
    // 2. 비밀번호 검증
    if (!passwordEncoder.matches(request.password(), userAuthInfo.password())) {
        throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    
    // 3. JWT 토큰 생성
    String accessToken = tokenPort.generateAccessToken(
        userAuthInfo.userId(), 
        userAuthInfo.email(), 
        userAuthInfo.roleNames()
    );
    
    return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
}
```

#### 3.2 권한 기반 접근 제어 🛡️

**역할 체계**:
```java
public enum RoleType {
    BUYER("구매자"),      // 기본 사용자
    SELLER("판매자"),     // 상품 판매자
    ADMIN("관리자");      // 시스템 관리자
}
```

**권한 매트릭스**:

| 기능 | BUYER | SELLER | ADMIN |
|------|-------|--------|-------|
| 회원가입 | ✅ | ✅ | ✅ |
| 내 정보 조회 | ✅ | ✅ | ✅ |
| 다른 회원 조회 | ❌ | ❌ | ✅ |
| 회원 상태 변경 | ❌ | ❌ | ✅ |
| 상품 등록 | ❌ | ✅ | ✅ |

#### 3.3 로그인 보안 (브루트포스 방어) 🛡️

**기능**: IP 기반 로그인 시도 횟수 제한으로 브루트포스 공격 방어

**보안 정책**:
- **최대 시도 횟수**: 5회
- **잠금 시간**: 15분 (900초)
- **추적 단위**: 클라이언트 IP 주소
- **메모리 기반**: `ConcurrentHashMap` 사용으로 빠른 응답

**동작 플로우**:
```java
@PostMapping("/auth/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
    String clientIp = getClientIp(httpServletRequest);
    
    // 1. IP 차단 상태 확인
    loginAttemptService.validateIpNotBlocked(clientIp);
    
    try {
        // 2. 로그인 시도
        AuthToken login = authUseCase.login(request);
        
        // 3. 성공 시 카운트 초기화
        loginAttemptService.recordSuccessfulLogin(clientIp);
        return ResponseEntity.ok(loginResponse);
        
    } catch (LoginFailedException e) {
        // 4. 실패 시 카운트 증가
        loginAttemptService.recordFailedAttempt(clientIp);
        throw e; // GlobalExceptionHandler로 전달
    }
}
```

**차단 로직**:
```java
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;      // 최대 시도 횟수
    private static final int LOCK_TIME_MINUTES = 15; // 잠금 시간(분)
    
    // IP별 시도 정보 추적
    private final ConcurrentHashMap<String, AttemptInfo> attemptCache = new ConcurrentHashMap<>();
    
    public void validateIpNotBlocked(String clientIp) {
        if (isBlocked(clientIp)) {
            throw new TooManyAttemptsException(/* 차단 정보 */);
        }
    }
}
```

**IP 추출 로직** (Proxy 환경 대응):
```java
private String getClientIp(HttpServletRequest request) {
    // 1. 테스트용 헤더 확인 (개발/테스트 환경)
    String testIp = request.getHeader("X-Test-Client-IP");
    if (testIp != null && !testIp.isEmpty()) {
        return testIp;
    }
    
    // 2. Proxy 헤더 확인 (운영 환경)
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        return xForwardedFor.split(",")[0].trim();
    }
    
    // 3. 기본 RemoteAddr
    return request.getRemoteAddr();
}
```

**에러 응답**:
```json
{
  "success": false,
  "code": "AUTH-002",
  "message": "너무 많은 로그인 시도로 15분간 차단되었습니다.",
  "timestamp": 1705520430000,
  "retryAfter": 900
}
```

**상태 코드**: `429 Too Many Requests`
**응답 헤더**: `Retry-After: 900` (초 단위)

#### 3.4 Gateway 연동 🌐

**헤더 기반 사용자 정보 전달**:

```bash
# Gateway → Member Service
GET /api/v1/members/123
X-User-ID: 2158078162337996800
X-EMAIL: user@example.com
X-ROLES: ROLE_BUYER,ROLE_SELLER
X-AUTH-METHOD: JWT
```

**ArgumentResolver를 통한 사용자 정보 추출**:
```java
@GetMapping("/{memberId}")
public ResponseEntity<MemberResponse> getMember(
        @PathVariable Long memberId,
        @CurrentUser AuthenticatedUser currentUser) {
    // Gateway에서 전달된 헤더 정보를 자동으로 AuthenticatedUser 객체로 변환
    return ResponseEntity.ok(memberUseCase.getMember(memberId));
}
```

---

## 📡 API 가이드

### 인증 API

#### 로그인
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**성공 응답 (200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**실패 응답**:

*로그인 실패 (401 Unauthorized)*:
```json
{
  "success": false,
  "code": "AUTH-001",
  "message": "로그인에 실패하였습니다. 이메일과 비밀번호를 확인해주세요.",
  "timestamp": 1705520430000
}
```

*브루트포스 차단 (429 Too Many Requests)*:
```json
{
  "success": false,
  "code": "AUTH-002",
  "message": "너무 많은 로그인 시도로 15분간 차단되었습니다.",
  "timestamp": 1705520430000
}
```
```http
Retry-After: 900
```

#### 로그아웃
```http
POST /auth/logout
Authorization: Bearer {accessToken}
```

### 회원 관리 API

#### 회원 검색 (관리자만)
```http
GET /members?keyword=김철수&status=ACTIVE&role=BUYER&page=0&size=20
Authorization: Bearer {accessToken}
```

**쿼리 파라미터**:
- `keyword` (optional): 통합 검색 (이름, 이메일)
- `name` (optional): 이름 검색
- `email` (optional): 이메일 검색
- `status` (optional): 회원 상태 (ACTIVE, INACTIVE, SUSPENDED, WITHDRAWN)
- `role` (optional): 권한 역할 (BUYER, SELLER, ADMIN)
- `page` (optional): 페이지 번호 (default: 0)
- `size` (optional): 페이지 크기 (default: 20, max: 100)

**성공 응답 (200 OK)**:
```json
{
  "content": [
    {
      "id": 2158078162337996800,
      "email": "user@example.com",
      "name": "김철수",
      "birthDate": "1990-01-01",
      "gender": "MALE",
      "status": "ACTIVE",
      "roles": ["BUYER", "SELLER"],
      "createdAt": "2025-01-18T10:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false,
  "empty": false
}
```

**권한 부족 (403 Forbidden)**:
```json
{
  "success": false,
  "code": "ACCESS-001",
  "message": "접근 권한이 없습니다. 관리자만 회원 검색이 가능합니다.",
  "timestamp": 1705520430000
}
```

#### 회원 가입
```http
POST /members
Content-Type: application/json

{
  "email": "newuser@example.com",
  "name": "홍길동",
  "password": "password123",
  "birthDate": "1990-01-01",
  "gender": "MALE"
}
```

#### 내 정보 조회
```http
GET /members/me
Authorization: Bearer {accessToken}
```

#### 특정 회원 조회
```http
GET /members/{memberId}
Authorization: Bearer {accessToken}
```

### 응답 형식

**성공 응답**:
```json
{
  "memberId": 2158078162337996800,
  "email": "user@example.com",
  "name": "홍길동",
  "birthDate": "1990-01-01",
  "gender": "MALE",
  "status": "ACTIVE",
  "createdAt": "2025-01-18T10:30:00",
  "roles": ["BUYER"]
}
```

**에러 응답**:
```json
{
  "success": false,
  "code": "MEMBER-001",
  "message": "회원을 찾을 수 없습니다.",
  "timestamp": 1705520430000,
  "traceId": "abc123..."
}
```

---

## 🚀 개발 환경 설정

### 사전 요구 사항

- **Java 21** 이상
- **PostgreSQL 16** (운영) / **H2** (테스트)
- **Spring Boot 3.5.3**

### 로컬 실행

#### 1. 환경 변수 설정

```bash
# 필수 환경 변수
export JWT_SECRET="your-super-secret-key-at-least-512-bits-long"
export db_url="jdbc:postgresql://localhost:5432/commerce_member"
export db_username="commerce_user"
export db_password="your_password"
export JASYPT_ENCRYPTOR_PASSWORD="encryption_key"
```

#### 2. 데이터베이스 실행

```bash
# PostgreSQL 실행 (Docker)
docker run -d \
  --name postgres-member \
  -e POSTGRES_DB=commerce_member \
  -e POSTGRES_USER=commerce_user \
  -e POSTGRES_PASSWORD=your_password \
  -p 5432:5432 \
  postgres:16
```

#### 3. 애플리케이션 실행

```bash
# 프로젝트 루트에서
cd service/member
./gradlew bootRun
```

#### 4. Health Check

```bash
curl http://localhost:8080/api/v1/auth/health
# 응답: "인증 서버가 정상 작동 중입니다."
```

### IDE 설정

#### IntelliJ IDEA
1. **Project Structure** → **SDK**: Java 21
2. **Gradle** → **Build and run using**: Gradle
3. **Annotation Processing** 활성화 (Lombok)

#### VS Code
```json
// .vscode/settings.json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic"
}
```

---

## 🗄️ 데이터베이스

### ERD (Entity Relationship Diagram)

```mermaid
erDiagram
    MEMBER {
        bigint id PK "Snowflake ID"
        varchar email UK "이메일 (고유)"
        varchar name "이름"
        varchar password "암호화된 비밀번호"
        date birth_date "생년월일"
        varchar gender "성별"
        varchar status "회원 상태"
        timestamp create_at "생성 시간"
    }
    
    MEMBER_ROLE {
        bigint id PK
        bigint member_id FK "회원 ID"
        varchar role_type "역할 타입"
        timestamp created_at "할당 시간"
    }
    
    MEMBER ||--o{ MEMBER_ROLE : "has roles"
```

### 테이블 상세

#### MEMBER 테이블
```sql
CREATE TABLE member (
    id            BIGINT       PRIMARY KEY,           -- Snowflake ID
    email         VARCHAR(255) NOT NULL UNIQUE,      -- 이메일
    name          VARCHAR(100) NOT NULL,             -- 이름
    password      VARCHAR(255) NOT NULL,             -- BCrypt 해시
    birth_date    DATE,                               -- 생년월일
    gender        VARCHAR(10)  NOT NULL,             -- MALE/FEMALE
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- 회원 상태
    create_at     TIMESTAMP    NOT NULL DEFAULT NOW() -- 생성 시간
);
```

#### MEMBER_ROLE 테이블
```sql
CREATE TABLE member_role (
    id          BIGINT      PRIMARY KEY,
    member_id   BIGINT      NOT NULL,
    role_type   VARCHAR(50) NOT NULL,              -- BUYER/SELLER/ADMIN
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    
    FOREIGN KEY (member_id) REFERENCES member(id)
);
```

### 테스트 데이터

```bash
# 테스트 데이터 삽입
psql -d commerce_member -f service/member/insert-test-data.sql
```

---

## 🛡️ 보안

### 비밀번호 보안

**BCrypt 암호화**:
```java
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {
    
    private static final int STRENGTH = 12;  // 보안 강도
    private final BCrypt.Hasher hasher = BCrypt.withDefaults();
    
    @Override
    public String encode(String rawPassword) {
        validatePasswordPolicy(rawPassword);  // 정책 검증
        return hasher.hashToString(STRENGTH, rawPassword.toCharArray());
    }
}
```

**비밀번호 정책**:
- 최소 8자 이상
- 영문, 숫자, 특수문자 포함
- 연속된 문자 금지

### JWT 보안

**토큰 설정**:
```yaml
jwt:
  secret: ${JWT_SECRET}                    # 512비트 이상 시크릿
  access-token-expiry: 3600000            # 1시간
  refresh-token-expiry: 604800000         # 7일
```

**보안 헤더**:
```java
return ResponseEntity.ok()
    .header("X-Content-Type-Options", "nosniff")
    .header("X-Frame-Options", "DENY")
    .header("Cache-Control", "no-store, no-cache, must-revalidate")
    .body(loginResponse);
```

### 브루트포스 방어

**로그인 시도 제한**:
```java
@Component
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;        // 최대 시도 횟수
    private static final int LOCK_TIME_MINUTES = 15;  // 잠금 시간(분)
    
    // Thread-safe 메모리 저장소
    private final ConcurrentHashMap<String, AttemptInfo> attemptCache = new ConcurrentHashMap<>();
    
    public void validateIpNotBlocked(String clientIp) {
        AttemptInfo attempt = attemptCache.get(clientIp);
        if (attempt != null && attempt.isBlocked()) {
            throw new TooManyAttemptsException(
                AuthErrorCode.TOO_MANY_ATTEMPTS,
                clientIp,
                attempt.getAttemptCount(),
                LOCK_TIME_MINUTES,
                "IP가 차단되었습니다"
            );
        }
    }
}
```

**차단 알고리즘**:
- **추적 단위**: 클라이언트 IP 주소
- **저장소**: 메모리 기반 (`ConcurrentHashMap`)
- **성능**: O(1) 조회 시간
- **안전성**: Thread-safe 동시성 보장
- **정책**: 실패 5회 → 15분 차단

**모니터링**:
```java
// 현재 상태 확인
int currentAttempts = loginAttemptService.getCurrentAttempts(clientIp);
boolean isBlocked = loginAttemptService.isBlocked(clientIp);

// 로깅
log.warn("🚨 브루트포스 공격 감지: ip={}, 시도횟수={}/{}", 
         clientIp, currentAttempts, MAX_ATTEMPTS);
```

### 권한 검증

**메서드 레벨 보안**:
```java
@PreAuthorize("hasRole('ADMIN') or #memberId == authentication.principal.userId")
public MemberResponse getMember(Long memberId) {
    // 관리자이거나 본인만 접근 가능
}
```

---

## 🧪 테스트

### 테스트 구조

```
src/test/java/
├── application/          # 애플리케이션 서비스 테스트
├── domain/              # 도메인 모델 테스트
├── infrastructure/      # 어댑터 테스트
└── integration/         # 통합 테스트
```

### 단위 테스트

#### 도메인 테스트
```java
@Test
void 회원_생성_성공() {
    // Given
    String email = "test@example.com";
    String name = "홍길동";
    
    // When
    Member member = Member.create(email, name, "password", "1990-01-01", "MALE");
    
    // Then
    assertThat(member.getEmail().email()).isEqualTo(email);
    assertThat(member.getName()).isEqualTo(name);
    assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
}
```

#### 서비스 테스트
```java
@ExtendWith(MockitoExtension.class)
class MemberApplicationServiceTest {
    
    @Mock private MemberRepository memberRepository;
    @Mock private PasswordEncoderPort passwordEncoder;
    
    @Test
    void 회원가입_성공() {
        // Given
        MemberCreateRequest request = new MemberCreateRequest(
            "test@example.com", "홍길동", "password", "1990-01-01", "MALE"
        );
        
        // When & Then
        assertThatNoException().isThrownBy(() -> {
            memberService.createMember(request);
        });
    }
}
```

### 통합 테스트

#### 컨트롤러 테스트
```java
@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)  // 필수!
class MemberControllerTest {

    @Test
    void 회원조회_성공() throws Exception {
        mockMvc.perform(get("/members/1")
                        .header("X-User-ID", "1")
                        .header("X-EMAIL", "test@test.com")
                        .header("X-ROLES", "BUYER"))
                .andExpect(status().isOk());
    }
}
```

#### 브루트포스 방어 테스트
```java
@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {
    
    @InjectMocks LoginAttemptService loginAttemptService;
    
    @Test
    void 로그인_5번_실패_후_차단() {
        String clientIp = "192.168.1.100";
        
        // Given: 4번 실패
        for (int i = 0; i < 4; i++) {
            loginAttemptService.recordFailedAttempt(clientIp);
            assertThat(loginAttemptService.isBlocked(clientIp)).isFalse();
        }
        
        // When: 5번째 실패
        loginAttemptService.recordFailedAttempt(clientIp);
        
        // Then: 차단됨
        assertThat(loginAttemptService.isBlocked(clientIp)).isTrue();
        assertThatThrownBy(() -> loginAttemptService.validateIpNotBlocked(clientIp))
                .isInstanceOf(TooManyAttemptsException.class);
    }
    
    @Test
    void 성공_로그인_시_카운트_초기화() {
        String clientIp = "192.168.1.100";
        
        // Given: 3번 실패
        for (int i = 0; i < 3; i++) {
            loginAttemptService.recordFailedAttempt(clientIp);
        }
        assertThat(loginAttemptService.getCurrentAttempts(clientIp)).isEqualTo(3);
        
        // When: 성공 로그인
        loginAttemptService.recordSuccessfulLogin(clientIp);
        
        // Then: 카운트 초기화
        assertThat(loginAttemptService.getCurrentAttempts(clientIp)).isEqualTo(0);
        assertThat(loginAttemptService.isBlocked(clientIp)).isFalse();
    }
}
```

#### 인증 통합 테스트
```java
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class})
class AuthControllerBruteForceTest {
    
    @MockitoBean LoginAttemptService loginAttemptService;
    @MockitoBean AuthUseCase authUseCase;
    
    @Test
    void 차단된_IP에서_로그인_시도() throws Exception {
        // Given: IP 차단 상태
        String blockedIp = "192.168.1.100";
        doThrow(new TooManyAttemptsException(
                AuthErrorCode.TOO_MANY_ATTEMPTS, 
                blockedIp, 5, 15, "차단됨"
        )).when(loginAttemptService).validateIpNotBlocked(blockedIp);
        
        LoginRequest request = new LoginRequest("test@test.com", "password");
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Forwarded-For", blockedIp))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "900"))
                .andExpect(jsonPath("$.code").value("AUTH-002"));
        
        // 차단되어서 실제 로그인 시도 안함
        verify(authUseCase, never()).login(any());
    }
}
```

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests MemberControllerTest

# 테스트 커버리지
./gradlew jacocoTestReport
```

---

## 📊 모니터링 & 트레이싱

### Health Check

```bash
# 기본 헬스 체크
curl http://localhost:8080/api/v1/auth/health

# Actuator 엔드포인트
curl http://localhost:8080/actuator/health
```

### 로깅

**로그 레벨 설정**:
```yaml
logging:
  level:
    innercircle.member: DEBUG
    org.springframework.security: DEBUG
  pattern:
    level: "%5p [${spring.application.name},%X{traceId:-},%X{spanId:-}]"
```

**주요 로그 포인트**:
```java
// 인증 성공/실패
log.info("🔐 로그인 성공: email={}, ip={}", email, clientIp);
log.warn("🚫 로그인 실패: email={}, ip={}, reason={}", email, clientIp, reason);

// 브루트포스 방어
log.warn("🚨 브루트포스 공격 감지: ip={}, 시도횟수={}/{}", 
         clientIp, currentAttempts, MAX_ATTEMPTS);
log.warn("⛔ IP 차단됨: ip={}, 시도횟수={}, 잠금시간={}분", 
         clientIp, attemptCount, lockTimeMinutes);
log.info("✅ 로그인 성공으로 차단 해제: ip={}", clientIp);

// 권한 체크
log.warn("🛡️ 권한 부족: 사용자={}, 요청={}", userId, endpoint);

// 회원 상태 변경
log.info("👤 회원 상태 변경: ID={}, {} → {}", memberId, oldStatus, newStatus);

// IP 추출 (개발/디버깅용)
log.debug("🌐 클라이언트 IP 추출: X-Forwarded-For={}, RemoteAddr={}, 최종IP={}", 
          xForwardedFor, remoteAddr, finalIp);
```

### 분산 트레이싱 (Micrometer Tracing)

**트레이싱 설정**:
```yaml
# application.yml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0          # 100% 샘플링 (개발환경)
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

**트레이스 정보 포함 로그**:
```yaml
logging:
  pattern:
    level: "%5p [${spring.application.name},%X{traceId:-},%X{spanId:-}]"
```

**실제 로그 출력**:
```bash
INFO [member,68b335a4ae975f2a6c2f0975832beaca,d4c504749c710079] - 🔍 회원 검색 수행
INFO [member,68b335a4ae975f2a6c2f0975832beaca,76b0eb46505663b7] - ✅ 로그인 성공: email=user@test.com
```

**자동 트레이스 대상**:
- **HTTP 요청/응답**: Controller 레벨 자동 추적
- **데이터베이스 쿼리**: JPA/JDBC 쿼리 실행 시간
- **외부 API 호출**: Gateway와의 통신

**커스텀 스팬 추가**:
```java
@Service
public class MemberApplicationService {
    
    public Page<MemberSearchResponse> searchMembers(MemberSearchRequest request) {
        // 자동으로 스팬 생성: "MemberApplicationService.searchMembers"
        
        // 커스텀 태그 추가
        Span.current()
            .setTag("search.keyword", request.keyword())
            .setTag("search.page", String.valueOf(request.page()))
            .setTag("search.size", String.valueOf(request.size()));
            
        Page<Member> members = memberQueryPort.searchMembers(...);
        
        // 결과 정보 추가
        Span.current()
            .setTag("search.results", String.valueOf(members.getTotalElements()));
            
        return members.map(this::memberToSearchResponse);
    }
}
```

**Zipkin UI에서 확인 가능한 정보**:
- **요청 흐름**: Gateway → Member Service
- **실행 시간**: 각 메서드별 소요 시간
- **DB 쿼리**: 실행된 SQL과 소요 시간  
- **에러 정보**: 예외 발생 시 스택 트레이스
- **커스텀 태그**: 비즈니스 정보 (검색 조건, 결과 수 등)

### 메트릭스

**커스텀 메트릭스**:
```java
@Component
public class MemberMetrics {
    
    @EventListener
    public void onMemberCreated(MemberCreatedEvent event) {
        Metrics.counter("member.created", "role", event.getRole()).increment();
    }
    
    @EventListener  
    public void onLoginAttempt(LoginAttemptEvent event) {
        Metrics.counter("auth.login.attempt", 
            "result", event.isSuccess() ? "success" : "failure"
        ).increment();
    }
    
    // 브루트포스 방어 메트릭스
    @EventListener
    public void onBruteForceAttempt(BruteForceAttemptEvent event) {
        Metrics.counter("auth.brute_force.attempt", 
            "ip", event.getClientIp(),
            "status", event.isBlocked() ? "blocked" : "allowed"
        ).increment();
    }
    
    @EventListener
    public void onIpBlocked(IpBlockedEvent event) {
        Metrics.counter("auth.ip.blocked").increment();
        Metrics.gauge("auth.ip.blocked_count", 
            loginAttemptService.getBlockedIpCount());
    }
}
```

**주요 메트릭스**:
- `auth.login.attempt` - 로그인 시도 횟수 (성공/실패별)
- `auth.brute_force.attempt` - 브루트포스 시도 횟수 (IP별, 차단여부별)
- `auth.ip.blocked` - IP 차단 발생 횟수
- `auth.ip.blocked_count` - 현재 차단된 IP 수
- `member.created` - 회원 가입 횟수 (역할별)

---

## 📚 관련 문서

### 아키텍처 & 설계
- 📖 [Member Domain 설계](docs/member-domain-design.md) - 도메인 모델 상세 설계
- 🏗️ [Service Discovery 비교](docs/service-discovery-comparison.md) - MSA 아키텍처 가이드

### 인증 & 보안
- 🔐 [JWT 구현 가이드](docs/JWT_IMPLEMENTATION_GUIDE.md) - JWT 인증 시스템 전체 구현 가이드
- 🛡️ [JWT 보안 가이드](docs/JWT_SECURITY_GUIDE.md) - 보안 위협과 방어 전략
- 🔄 [Refresh Token 가이드](docs/REFRESH_TOKEN_GUIDE.md) - 토큰 갱신 시스템 구현 및 사용법
- 🔒 [JWT-Redis 인증](docs/jwt-redis-authentication.md) - Redis 기반 토큰 관리

### 인프라 & 설정
- 🐘 [PostgreSQL 설정](docs/postgresql-setup.md) - 데이터베이스 설정 가이드

---

## 🤝 기여 가이드

### 코딩 컨벤션

**패키지 구조**:
```
innercircle.member
├── application/     # 애플리케이션 레이어
├── domain/         # 도메인 레이어
└── infrastructure/ # 인프라스트럭처 레이어
```

**네이밍 컨벤션**:
- **클래스**: PascalCase (예: `MemberApplicationService`)
- **메서드**: camelCase (예: `createMember`)
- **상수**: UPPER_SNAKE_CASE (예: `DEFAULT_ROLE`)

### Git 워크플로우

```bash
# Feature 브랜치 생성
git checkout -b feature/member-profile-update

# 커밋 메시지 규칙
git commit -m "feat: 회원 프로필 수정 기능 추가"
git commit -m "fix: 비밀번호 검증 오류 수정"
git commit -m "docs: API 가이드 업데이트"
```

### Pull Request

1. **기능 단위로 작은 PR** 생성
2. **테스트 코드** 포함 필수
3. **문서 업데이트** (API 변경 시)
4. **리뷰 요청** 전 자체 테스트 완료

---

## 🐛 트러블슈팅

### 자주 발생하는 문제

#### 1. JWT 토큰 검증 실패
```bash
# 증상
"Failed to validate the token"

# 원인
- Gateway와 Member Service의 JWT Secret 불일치
- 토큰 만료
- 알고리즘 불일치 (HS256 vs HS512)

# 해결책
1. JWT_SECRET 환경변수 확인
2. 토큰 만료시간 확인  
3. 알고리즘 통일 (HS512 권장)
```

#### 2. 403 Forbidden 에러
```bash
# 증상
테스트에서 403 에러 발생

# 원인
@WebMvcTest에 SecurityConfig import 누락

# 해결책
@Import(SecurityConfig.class) 추가
```

#### 3. 데이터베이스 연결 실패
```bash
# 증상
Connection refused to PostgreSQL

# 해결책
1. PostgreSQL 서버 실행 확인
2. 환경변수 설정 확인
3. 방화벽 설정 확인
```

#### 4. PostgreSQL JPQL 호환성 문제
```bash
# 증상 1: @Embedded 필드 에러
"ERROR: function lower(bytea) does not exist"

# 원인
Hibernate가 @Embedded Email 객체를 bytea로 잘못 매핑

# 해결책
1. Native Query 사용 (권장)
@Query(value = "SELECT * FROM member WHERE LOWER(email) LIKE ...", nativeQuery = true)

2. 컬럼 정의 명시
@Column(columnDefinition = "VARCHAR(150)")

3. toString() 메서드 추가
@Override public String toString() { return email; }

# 증상 2: Enum 비교 에러
"No function matches the given name and argument types"

# 원인
PostgreSQL에서 Enum 타입 비교 시 캐스팅 필요

# 해결책
1. Native Query에서 캐스팅
WHERE m.status::text = :status

2. 파라미터를 String으로 변경
@Param("status") String status
```

#### 5. 브루트포스 차단 관련 문제
```bash
# 증상 1: 정상 사용자가 차단됨
"Too many login attempts. IP blocked for 15 minutes."

# 원인
- 동일 IP에서 여러 사용자가 로그인 시도
- 개발 환경에서 모든 요청이 127.0.0.1로 인식

# 해결책
1. IP 추출 로직 확인 (X-Forwarded-For 헤더)
2. 개발 환경: X-Test-Client-IP 헤더 사용
3. 필요시 특정 IP 화이트리스트 추가

# 증상 2: 테스트에서 IP가 127.0.0.1로 고정됨
MockMvc 테스트에서 실제 IP 추출 불가

# 해결책
@Test
void 브루트포스_테스트() throws Exception {
    mockMvc.perform(post("/auth/login")
            .header("X-Test-Client-IP", "192.168.1.100")  // 테스트용 IP
            .content(...))
            .andExpect(status().isTooManyRequests());
}

# 증상 3: 메모리 사용량 증가
ConcurrentHashMap에 차단 정보 누적

# 원인
만료된 차단 정보가 정리되지 않음

# 해결책
1. 주기적 정리 작업 확인 (15분마다 실행)
2. 메모리 사용량 모니터링
3. 필요시 TTL 기반 캐시로 변경 고려
```

### 로그 분석

**디버그 모드 활성화**:
```yaml
logging:
  level:
    innercircle.member: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
```

---

### 개발팀 연락처
- **Team Lead**: Commerce 개발팀
- **Repository**: [GitHub - Commerce Platform](https://github.com/INNER-CIRCLE-ICD4/Project-3.-Commerce)
- **Issues**: GitHub Issues 활용

### 긴급 상황
- **Production Issue**: Slack #commerce-alerts
- **Security Issue**: 보안팀 직접 연락

---

**Last Updated**: 2025-08-31  
**Version**: 2.0.0  
**Maintainer**: Commerce 개발팀

### 🆕 v2.0.0 주요 변경사항
- ✅ **회원 검색 기능**: 복합 조건 검색 + 페이징 지원  
- ✅ **CQRS 패턴**: Command/Query 책임 분리
- ✅ **분산 트레이싱**: Micrometer + Zipkin 통합
- ✅ **PostgreSQL 호환성**: Native Query 지원
- ✅ **성능 최적화**: 쿼리 분리 + 배치 로딩
