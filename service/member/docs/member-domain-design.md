# 회원 도메인 설계 문서

## 📋 목차
- [1. 프로젝트 개요](#1-프로젝트-개요)
- [2. 클린 아키텍처 접근법](#2-클린-아키텍처-접근법)
- [3. 패키지 구조](#3-패키지-구조)
- [4. 도메인 모델 설계](#4-도메인-모델-설계)
- [5. 레이어별 구현](#5-레이어별-구현)
- [6. 개발 순서](#6-개발-순서)
- [7. DomainService vs ApplicationService](#7-domainservice-vs-applicationservice)

## 1. 프로젝트 개요

### 1.1 기술 스택
- **Backend**: Spring Boot, JPA, QueryDSL, WebFlux
- **Database**: MariaDB
- **Infrastructure**: Kubernetes, Eureka, Spring Cloud Gateway
- **ID Generation**: Snowflake Algorithm

### 1.2 회원 도메인 범위
- **구매자 (BUYER)**: 상품 구매, 리뷰 작성
- **판매자 (SELLER)**: 상품 등록, 주문 관리
- **관리자 (ADMIN)**: 시스템 관리, 판매자 승인

### 1.3 아키텍처 선택
**클린 아키텍처**를 채택하되, **단순한 3-layer 구조**로 시작하여 점진적으로 발전시키는 접근법을 사용합니다.

## 2. 클린 아키텍처 접근법

### 2.1 기본 원칙: 안에서 밖으로 (Inside-Out)
```
1. Domain (Core) 먼저
2. Application Layer 
3. Infrastructure Layer
```

### 2.2 의존성 규칙
```
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure                           │
│  (Controller, JPA, Email, Config)                         │
│                        ↓                                   │
├─────────────────────────────────────────────────────────────┤
│                    Application                             │
│  (UseCase, Service, Command, Response, Port)               │
│                        ↓                                   │
├─────────────────────────────────────────────────────────────┤
│                     Domain                                 │
│  (Entity, Value Object, Repository Interface, Domain Service) │
└─────────────────────────────────────────────────────────────┘
```

**의존성 규칙:**
- ✅ Infrastructure → Application → Domain (허용)
- ❌ Domain → Application (금지)
- ❌ Domain → Infrastructure (금지)

### 2.3 DomainService vs ApplicationService 구분

| 구분 | DomainService | ApplicationService |
|------|---------------|-------------------|
| **위치** | Domain Layer | Application Layer |
| **역할** | 순수 비즈니스 로직 | 유스케이스 조율 |
| **의존성** | 도메인 객체만 | 인프라까지 가능 |
| **트랜잭션** | 모름 | 관리함 |
| **외부 시스템** | 모름 | 호출함 |

## 3. 패키지 구조

### 3.1 단순한 3-Layer 구조
```java
src/main/java/innercircle/commerce/member/
├── domain/                    # 🏛️ Domain Layer
│   ├── Member.java           # 엔티티
│   ├── Email.java            # 값 객체
│   ├── MemberRole.java       # 엔티티
│   ├── MemberRepository.java # 리포지토리 인터페이스
│   ├── MemberDomainService.java # 도메인 서비스
│   └── MemberRegisteredEvent.java # 도메인 이벤트
├── application/               # 🔄 Application Layer  
│   ├── MemberUseCase.java    # 유스케이스 인터페이스
│   ├── MemberService.java    # 유스케이스 구현체
│   ├── RegisterMemberCommand.java # 커맨드
│   ├── MemberResponse.java   # 응답 DTO
│   └── NotificationPort.java # 외부 포트 인터페이스
└── infrastructure/            # 🔧 Infrastructure Layer
    ├── JpaMemberRepository.java # JPA 구현체
    ├── MemberController.java    # 웹 컨트롤러
    ├── EmailNotificationAdapter.java # 외부 서비스 어댑터
    └── MemberConfig.java        # 설정
```

### 3.2 점진적 발전 계획
```java
// 현재 (단순)
application/
├── MemberUseCase.java
├── MemberService.java
└── MemberResponse.java

// 나중에 (필요시 세분화)
application/
├── port/
│   ├── in/
│   └── out/
├── service/
├── command/
└── response/
```

## 4. 도메인 모델 설계

### 4.1 회원 기본 정보

```java
@Entity
@Table(name = "members")
@NaturalIdCache
public class Member extends BaseEntity {
    
    @NaturalId
    @Column(name = "email", nullable = false)
    @Embedded
    private Email email;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;
    
    // Static Factory Method
    public static Member create(String email, String name, String password) {
        return new Member(new Email(email), name, password);
    }
    
    // 비즈니스 메서드
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
}
```

### 4.2 Email 값 객체 (Record 사용)

```java
public record Email(String value) {
    
    public Email {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        // 정규화: 소문자 + 공백 제거
        value = value.toLowerCase().trim();
        
        // 유효성 검증
        if (!isValidEmail(value)) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }
    
    private static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
```

### 4.3 역할 관리

```java
@Entity
@Table(name = "member_roles")
public class MemberRole {
    @Id
    private Long memberRoleId;
    
    private Long memberId;
    
    @Enumerated(EnumType.STRING)
    private RoleType roleType;  // BUYER, SELLER, ADMIN
    
    @Enumerated(EnumType.STRING)
    private RoleStatus status;  // ACTIVE, PENDING, SUSPENDED
    
    private LocalDateTime assignedAt;
    private LocalDateTime approvedAt;
}

public enum RoleType {
    BUYER("구매자"),
    SELLER("판매자"),
    ADMIN("관리자");
    
    private final String description;
    
    RoleType(String description) {
        this.description = description;
    }
}
```

### 4.4 BaseEntity 설계

```java
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    private Long memberId;

    // JPA용 기본 생성자 (ID 생성 안함)
    protected BaseEntity() {
        // 기본 생성자에서는 ID 생성하지 않음
    }

    // 저장 직전에만 ID 생성 (안전함)
    @PrePersist
    private void generateId() {
        if (this.memberId == null) {
            this.memberId = SnowFlakeGenerator.GENERATOR.nextId();
        }
    }

    public Long getMemberId() {
        return memberId;
    }

    // equals/hashCode는 서브클래스에서 구현
}
```

### 4.5 equals/hashCode 구현

```java
// Email 기반 구현 (권장)
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    
    Member member = (Member) obj;
    return Objects.equals(email, member.email);
}

@Override
public int hashCode() {
    return Objects.hash(email);
}
```

## 5. 레이어별 구현

### 5.1 Domain Layer

#### 도메인 서비스
```java
@Component
public class MemberDomainService {
    
    /**
     * 순수한 비즈니스 로직만 담당
     * - 외부 시스템을 모름
     * - 트랜잭션을 모름  
     * - 도메인 규칙만 검증
     */
    
    public void validateDuplicateEmail(Email email, MemberRepository repository) {
        if (repository.existsByEmail(email)) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + email.value());
        }
    }
    
    public boolean canUpgradeToSeller(Member member) {
        return member.getStatus() == MemberStatus.ACTIVE 
            && member.getCreatedAt().isBefore(LocalDateTime.now().minusDays(30));
    }
    
    public void validatePasswordPolicy(String password, Member member) {
        if (password.length() < 8) {
            throw new InvalidPasswordException("비밀번호는 8자 이상이어야 합니다");
        }
        
        if (password.equals(member.getName())) {
            throw new InvalidPasswordException("비밀번호는 이름과 같을 수 없습니다");
        }
    }
}
```

#### Repository 인터페이스
```java
public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long memberId);
    Optional<Member> findByEmail(Email email);
    boolean existsByEmail(Email email);
}
```

### 5.2 Application Layer

#### 유스케이스 인터페이스
```java
public interface MemberUseCase {
    MemberResponse registerMember(RegisterMemberCommand command);
    MemberResponse getMember(Long memberId);
    void updateMember(UpdateMemberCommand command);
}
```

#### 애플리케이션 서비스
```java
@Service
@Transactional
public class MemberService implements MemberUseCase {

    private final MemberRepository memberRepository;
    private final MemberDomainService memberDomainService;
    private final NotificationPort notificationPort;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 유스케이스 조율 담당
     * - 트랜잭션 관리
     * - 외부 시스템 호출
     * - 도메인 서비스 조율
     * - 인프라 계층 사용
     */

    @Override
    public MemberResponse registerMember(RegisterMemberCommand command) {
        
        // 1. 도메인 서비스를 통한 검증
        Email email = new Email(command.email());
        memberDomainService.validateDuplicateEmail(email, memberRepository);
        
        // 2. 인프라 계층 사용 (비밀번호 암호화)
        String encodedPassword = passwordEncoder.encode(command.password());
        
        // 3. 도메인 객체 생성
        Member member = Member.create(
            command.email(),
            command.name(),
            encodedPassword
        );
        
        // 4. 저장 (트랜잭션 내에서)
        Member savedMember = memberRepository.save(member);
        
        // 5. 외부 시스템 호출 (이메일 발송)
        notificationPort.sendWelcomeEmail(email, command.name());
        
        // 6. 도메인 이벤트 발행
        eventPublisher.publishEvent(
            new MemberRegisteredEvent(savedMember.getId(), email)
        );
        
        return MemberResponse.from(savedMember);
    }
    
    @Override
    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다"));
        return MemberResponse.from(member);
    }
}
```

#### 커맨드와 응답 객체
```java
// 커맨드
public record RegisterMemberCommand(
    String email,
    String name,
    String password
) {}

// 응답 DTO
public record MemberResponse(
    Long memberId,
    String email,
    String name,
    MemberStatus status,
    LocalDateTime createdAt
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
            member.getMemberId(),
            member.getEmail().value(),
            member.getName(),
            member.getStatus(),
            member.getCreatedAt()
        );
    }
}
```

#### 외부 포트 인터페이스
```java
public interface NotificationPort {
    void sendWelcomeEmail(Email email, String name);
    void sendPasswordResetEmail(Email email, String resetToken);
}
```

### 5.3 Infrastructure Layer

#### JPA Repository 구현체
```java
@Repository
public class JpaMemberRepository implements MemberRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Member save(Member member) {
        entityManager.persist(member);
        return member;
    }

    @Override
    public Optional<Member> findByEmail(Email email) {
        String jpql = "SELECT m FROM Member m WHERE m.email.value = :email";
        List<Member> results = entityManager.createQuery(jpql, Member.class)
            .setParameter("email", email.value())
            .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByEmail(Email email) {
        String jpql = "SELECT COUNT(m) FROM Member m WHERE m.email.value = :email";
        Long count = entityManager.createQuery(jpql, Long.class)
            .setParameter("email", email.value())
            .getSingleResult();
        return count > 0;
    }
}
```

#### 웹 컨트롤러
```java
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberUseCase memberUseCase;

    public MemberController(MemberUseCase memberUseCase) {
        this.memberUseCase = memberUseCase;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> registerMember(
            @RequestBody @Valid RegisterMemberRequest request) {
        
        RegisterMemberCommand command = new RegisterMemberCommand(
            request.email(),
            request.name(),
            request.password()
        );
        
        MemberResponse response = memberUseCase.registerMember(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId) {
        MemberResponse response = memberUseCase.getMember(memberId);
        return ResponseEntity.ok(response);
    }
}
```

#### 외부 서비스 어댑터
```java
@Component
public class EmailNotificationAdapter implements NotificationPort {

    private final JavaMailSender mailSender;

    @Override
    public void sendWelcomeEmail(Email email, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email.value());
        message.setSubject("회원가입을 축하합니다!");
        message.setText(String.format("안녕하세요 %s님, 가입을 환영합니다!", name));
        
        mailSender.send(message);
    }
}
```

## 6. 개발 순서

### 6.1 단계별 개발 순서

#### 1단계: Domain 구현
```java
// 가장 먼저 도메인부터
Member.java           // 엔티티
Email.java            // 값 객체  
MemberRepository.java // 인터페이스
MemberDomainService.java // 도메인 서비스
```

#### 2단계: Application 구현
```java
// 유스케이스 정의
MemberUseCase.java    // 인터페이스
MemberService.java    // 구현체
RegisterMemberCommand.java // 커맨드
MemberResponse.java   // 응답
NotificationPort.java // 외부 포트
```

#### 3단계: Infrastructure 구현
```java
// 마지막에 기술적 구현
JpaMemberRepository.java    // JPA 구현
MemberController.java       // 컨트롤러
EmailNotificationAdapter.java // 외부 어댑터
MemberConfig.java          // 설정
```

### 6.2 반복 개발 사이클
```
1. 요구사항 분석 → 도메인 모델 수정
2. 도메인 테스트 작성 → 도메인 구현
3. 유스케이스 테스트 작성 → 애플리케이션 서비스 구현  
4. 인프라 테스트 작성 → 인프라 구현
5. API 테스트 작성 → 컨트롤러 구현
6. 통합 테스트 → 전체 플로우 검증
```

### 6.3 시작하기 좋은 순서
1. **가장 핵심적인 도메인 객체 1개**부터 시작
2. **단순한 CRUD 유스케이스** 먼저 구현
3. **복잡한 비즈니스 로직**은 나중에 추가
4. **외부 연동**은 마지막에 구현

## 7. DomainService vs ApplicationService

### 7.1 DomainService 예시

```java
// ✅ DomainService: 순수 비즈니스 로직만
@Component
public class MemberDomainService {
    
    public void validateRegistrationPolicy(Email email, int age) {
        if (email.isCompanyEmail() && age < 18) {
            throw new InvalidMemberException("미성년자는 회사 이메일로 가입할 수 없습니다");
        }
    }
}
```

### 7.2 ApplicationService 예시

```java
// ✅ ApplicationService: 유스케이스 조율
@Service
@Transactional
public class MemberService {
    
    public void registerMember(RegisterMemberCommand command) {
        // 도메인 서비스에 위임
        memberDomainService.validateRegistrationPolicy(
            new Email(command.email()), 
            command.age()
        );
        
        // 나머지 유스케이스 처리...
    }
}
```

### 7.3 주의사항

#### ❌ 잘못된 예시
```java
// ❌ DomainService에서 인프라 의존성 사용
@Service
public class MemberDomainService {
    
    @Autowired
    private EmailService emailService;  // ❌ 외부 시스템 의존
    
    @Transactional  // ❌ 트랜잭션 관리
    public void validateAndNotify(Member member) {
        emailService.send(member.getEmail());
    }
}
```

#### ✅ 올바른 예시
```java
// ✅ DomainService: 순수 비즈니스 로직만
@Component
public class MemberDomainService {
    
    public void validateRegistrationPolicy(Email email, int age) {
        // 순수한 도메인 규칙만
    }
}

// ✅ ApplicationService: 유스케이스 조율
@Service
@Transactional
public class MemberService {
    
    public void registerMember(RegisterMemberCommand command) {
        // 도메인 서비스 + 외부 시스템 조율
    }
}
```

## 8. 테스트 전략

### 8.1 도메인 테스트
```java
class MemberTest {
    
    @Test
    void 멤버_생성_시_이메일_유효성_검증() {
        assertThatThrownBy(() -> Member.create("invalid-email", "name", "password"))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void 비밀번호_변경_성공() {
        Member member = Member.create("test@test.com", "name", "password");
        
        member.changePassword("newPassword");
        
        assertThat(member.getPassword()).isEqualTo("newPassword");
    }
}
```

### 8.2 애플리케이션 서비스 테스트
```java
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private NotificationPort notificationPort;
    
    @Mock
    private MemberDomainService memberDomainService;

    @InjectMocks
    private MemberService memberService;

    @Test
    void 멤버_등록_성공() {
        // given
        RegisterMemberCommand command = new RegisterMemberCommand("test@test.com", "홍길동", "password123");
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // when
        MemberResponse response = memberService.registerMember(command);

        // then
        assertThat(response.email()).isEqualTo("test@test.com");
        verify(notificationPort).sendWelcomeEmail(any(Email.class), eq("홍길동"));
    }
}
```

## 9. 개발 팁

### 9.1 인터페이스 사용의 장점
- ✅ **의존성 역전**: 고수준 모듈이 저수준 모듈에 의존하지 않음
- ✅ **테스트 용이성**: Mock 객체로 쉽게 테스트 가능
- ✅ **구현체 교체**: 비즈니스 로직 변경 없이 구현 변경 가능
- ✅ **명확한 계약**: 인터페이스가 명확한 계약을 정의

### 9.2 주의사항
- ❌ 한 번에 모든 레이어를 구현하지 마세요
- ✅ **한 번에 하나의 유스케이스**씩 완성
- ✅ **테스트 먼저** 작성하는 습관
- ✅ **의존성 방향**을 항상 확인

---

**문서 작성일**: 2024년  
**작성자**: Member Domain Team  
**버전**: 2.0 (단순한 3-layer 구조)
