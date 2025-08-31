# 👥 Member Service

Commerce MSA 플랫폼의 **회원 관리 및 인증** 마이크로서비스입니다.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen?logo=spring-boot)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![JWT](https://img.shields.io/badge/JWT-0.12.3-purple)

---

## 📋 목차

- [개요](#-개요)
- [주요 기능](#-주요-기능)
- [아키텍처](#-아키텍처)
- [API 가이드](#-api-가이드)
- [개발 환경 설정](#-개발-환경-설정)
- [테스트](#-테스트)
- [모니터링](#-모니터링--트레이싱)

---

## 🎯 개요

### 책임과 역할

**Member Service**는 Commerce 플랫폼에서 다음과 같은 핵심 책임을 담당합니다:

- 👤 **회원 관리**: 사용자 등록, 프로필 관리, 회원 검색
- 🔐 **인증**: JWT 기반 로그인/로그아웃, 토큰 관리  
- 🛡️ **권한 관리**: 역할 기반 접근 제어 (RBAC), 권한 부여
- 🔒 **보안**: 비밀번호 암호화, 브루트포스 방어

### 기술 스택

| 영역 | 기술 | 버전 | 용도 |
|------|------|------|------|
| **Framework** | Spring Boot | 3.5.3 | 애플리케이션 프레임워크 |
| **Architecture** | Hexagonal + CQRS | - | 아키텍처 패턴 |
| **Security** | Spring Security + JWT | 6.x + 0.12.3 | 인증/인가 |
| **Database** | PostgreSQL | 16 | 운영 데이터베이스 |
| **ORM** | Spring Data JPA | 3.x | 데이터 접근 계층 |
| **Tracing** | Micrometer + Zipkin | 1.12.x | 분산 트레이싱 |

---

## 🚀 주요 기능

### 1. 회원 관리 👤

#### 1.1 회원 가입 & 로그인
- **회원가입**: 이메일 중복 검증, 비밀번호 암호화
- **로그인**: JWT 토큰 발행, 브루트포스 방어 (5회 실패 시 5분 차단)
- **토큰 관리**: Access Token(1시간) + Refresh Token(7일)

#### 1.2 회원 검색 🔍
- **복합 검색**: 이름, 이메일, 상태, 권한별 검색 지원
- **페이징**: 기본 20개씩, 최대 100개까지 조회 가능
- **권한**: ADMIN만 사용 가능

#### 1.3 권한 부여 🛡️
- **ADMIN 권한 부여**: 시스템 관리자만 부여 가능
- **SELLER 권한 부여**: 판매자 권한 부여 (상품 등록/관리)
- **중복 방지**: DB 제약조건 + 도메인 로직 이중 보장

### 2. 인증 시스템 🔐

#### 2.1 JWT 토큰 관리
```java
// Access Token (1시간)
{
  "sub": "user123",
  "email": "user@example.com", 
  "roles": ["BUYER"],
  "exp": 1640995200
}

// Refresh Token (7일)  
{
  "sub": "user123",
  "type": "refresh",
  "exp": 1641600000
}
```

#### 2.2 보안 정책
- **비밀번호**: BCrypt 해싱, 최소 8자리
- **로그인 제한**: IP별 5회 실패 시 5분 차단
- **토큰 무효화**: 로그아웃 시 Redis 블랙리스트 등록

### 3. 권한 체계 🔑

```java
public enum RoleType {
    BUYER("구매자"),      // 기본 회원
    SELLER("판매자"),     // 상품 판매자  
    ADMIN("관리자");      // 시스템 관리자
}
```

**권한 매트릭스**:

| 기능 | BUYER | SELLER | ADMIN |
|------|-------|--------|-------|
| 회원가입 | ✅ | ✅ | ✅ |
| 내 정보 조회 | ✅ | ✅ | ✅ |
| 회원 검색 | ❌ | ❌ | ✅ |
| 권한 부여 | ❌ | ❌ | ✅ |
| 상품 등록 | ❌ | ✅ | ✅ |

---

## 🏗️ 아키텍처

### 헥사고날 아키텍처 + CQRS 패턴

```
┌─────────────────────────────────────────────────────────────┐
│                    Member Service                           │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure Layer (Adapters)                            │
│  ├── in/  : AuthController, MemberController, RoleController│
│  ├── out/ : JpaRepository, JwtTokenAdapter, BCryptAdapter   │
│  └── persistence/ : MemberJpaRepository                     │
├─────────────────────────────────────────────────────────────┤
│  Application Layer (CQRS Use Cases)                         │
│  ├── service/ : MemberApplicationService                    │
│  ├── port/in/  : MemberUseCase (Command + Query)            │
│  ├── port/out/ : MemberCommandPort, MemberQueryPort         │
├─────────────────────────────────────────────────────────────┤
│  Domain Layer (Core Business Logic)                         │
│  ├── entity/ : Member, MemberRole, Email (Value Object)     │
│  ├── service/ : MemberDomainService                         │
│  ├── exception/ : BusinessException, DuplicateRoleException │
│  └── enum/ : RoleType, MemberStatus                         │
└─────────────────────────────────────────────────────────────┘
```

### 핵심 엔티티

```java
@Entity
@Table(name = "member")
public class Member extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Embedded
    private Email email;
    
    private String name;
    private String password;
    
    @Enumerated(EnumType.STRING)
    private MemberStatus status;
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MemberRole> roles = new HashSet<>();
    
    // 도메인 메서드
    public boolean hasRole(RoleType roleType) { /* ... */ }
    public void grantRole(RoleType roleType) { /* ... */ }
}
```

---

## 📡 API 가이드

### 인증 API

#### 로그인
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

#### 토큰 갱신
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 회원 API

#### 회원 검색 (관리자만)
```http
GET /api/v1/members?name=김철수&role=BUYER&page=0&size=20
Authorization: Bearer {accessToken}
```

### 권한 부여 API

#### ADMIN 권한 부여 (관리자만)
```http
POST /api/v1/grant/admin/{memberId}
Authorization: Bearer {accessToken}
```

#### SELLER 권한 부여 (관리자만) 
```http
POST /api/v1/grant/seller/{memberId}
Authorization: Bearer {accessToken}
```

---

## ⚙️ 개발 환경 설정

### 필수 사항
- **Java 21+**
- **PostgreSQL 16+**
- **Redis 6+** (선택사항)

### 로컬 실행
```bash
# 데이터베이스 준비
docker-compose up -d postgres

# 애플리케이션 실행
./gradlew bootRun

# 테스트 실행
./gradlew test
```

### 환경 변수
```properties
# application-local.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/member_db
    username: postgres
    password: password
    
  security:
    jwt:
      secret: your-256-bit-secret-key-here
      access-token-expiry: 3600  # 1시간
      refresh-token-expiry: 604800  # 7일
```

---

## 🧪 테스트

### 단위 테스트
```java
@ExtendWith(MockitoExtension.class)  
class MemberApplicationServiceTest {
    
    @Mock private MemberQueryPort memberQueryPort;
    @Mock private MemberCommandPort memberCommandPort; 
    @InjectMocks private MemberApplicationService memberService;
    
    @Test
    void ADMIN_권한_부여_성공() {
        // Given
        Member member = Member.create("user@test.com", "테스트유저", "password", "1990-01-01", "MALE");
        when(memberQueryPort.findById(1L)).thenReturn(Optional.of(member));
        
        // When
        MemberRole result = memberService.grantAdminRole(1L);
        
        // Then
        assertThat(result.getRoleType()).isEqualTo(RoleType.ADMIN);
        verify(memberCommandPort).save(member);
    }
}
```

### 통합 테스트
```java
@SpringBootTest
@Transactional
class MemberIntegrationTest {
    
    @Autowired private MemberApplicationService memberService;
    
    @Test
    void 회원_생성_및_권한_부여_통합_테스트() {
        // Given
        Member member = memberService.createMember(/* ... */);
        
        // When  
        MemberRole adminRole = memberService.grantAdminRole(member.getId());
        
        // Then
        assertThat(member.hasRole(RoleType.ADMIN)).isTrue();
    }
}
```

---

## 📊 모니터링 & 트레이싱

### 분산 트레이싱 (Zipkin)
- **Micrometer Tracing**: HTTP 요청, DB 쿼리, 외부 API 호출 추적
- **Zipkin UI**: http://localhost:9411
- **B3 Propagation**: MSA 간 TraceID/SpanID 전파

### 주요 메트릭스
- `auth.login.attempt` - 로그인 시도 횟수
- `auth.brute_force.blocked` - IP 차단 횟수  
- `role.granted` - 권한 부여 횟수
- `member.created` - 회원 가입 횟수

---

## 🔧 트러블슈팅

### 자주 발생하는 문제

#### PostgreSQL bytea 에러
```
ERROR: function lower(bytea) does not exist
```
**해결**: Native Query 사용 권장
```java
@Query(value = "SELECT * FROM member m WHERE LOWER(m.email) ILIKE LOWER(CONCAT('%', :email, '%'))", nativeQuery = true)
List<Member> searchByEmail(@Param("email") String email);
```

#### JWT 권한 매핑 이슈
```
Cannot convert existing claim value of type 'class java.util.ArrayList'
```
**해결**: JWT 클레임을 `String[]`로 저장, `List<String>`으로 읽기
```java
// 저장
claims.put("roles", roles.toArray(new String[0]));

// 읽기  
List<String> roles = Arrays.asList((String[]) claims.get("roles"));
```

---

## 📚 관련 문서

- [JWT 인증 가이드](./docs/jwt-redis-authentication.md)
- [회원 도메인 설계](./docs/member-domain-design.md) 
- [PostgreSQL 설정](./docs/postgresql-setup.md)

---

**Version**: 2.1.0  
**Last Updated**: 2025-08-31  
**Maintainer**: Commerce 개발팀

### 🆕 v2.1.0 주요 변경사항
- ✅ **권한 부여 시스템**: ADMIN/SELLER 권한 부여 API 추가
- ✅ **RoleController**: 전용 권한 관리 컨트롤러 구현
- ✅ **중복 권한 방지**: DB 제약조건 + 도메인 로직 이중 보장
- ✅ **분산 트레이싱**: Micrometer + Zipkin 완전 통합
- ✅ **PostgreSQL 최적화**: Native Query 기반 검색 성능 향상