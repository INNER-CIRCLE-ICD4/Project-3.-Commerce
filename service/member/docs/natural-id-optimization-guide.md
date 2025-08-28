# JPA @NaturalId 최적화 가이드

## 📋 개요

JPA에서 `@NaturalId`를 활용한 엔티티 조회 최적화 방법과 2차 캐시 활용 전략을 정리한 가이드입니다.

**작성일**: 2025-01-18  
**버전**: 1.0.0  
**대상**: Member Service 개발팀

---

## 🎯 @NaturalId란?

### 정의

**Natural ID**는 **업무적으로 의미가 있는 고유 식별자**를 의미합니다. 시스템에서 생성하는 기술적 ID(Primary Key)와 달리 사용자가 실제로 인식하고 사용하는 식별자입니다.

### Primary Key vs Natural ID

```java
@Entity
public class Member {
    @Id @GeneratedValue
    private Long id;              // 🔧 Primary Key (기술적 ID)
    
    @NaturalId
    @Embedded
    private Email email;          // 👤 Natural ID (업무적 ID)
}
```

| 구분 | Primary Key | Natural ID |
|------|-------------|------------|
| **목적** | 기술적 식별 | 업무적 식별 |
| **의미** | 시스템 내부용 | 사용자가 인식 가능 |
| **예시** | `id: 2158078162337996800` | `email: user@example.com` |
| **변경 가능성** | 불변 | 드물게 변경 |
| **캐시 최적화** | 기본 지원 | **특별 최적화** ✅ |

---

## 🏗️ 현재 프로젝트 적용 현황

### Member 엔티티 구현

```java
@Entity
@Table(name = "member", uniqueConstraints = {
    @UniqueConstraint(name = "UK_MEMBER_EMAIL_ADDRESS", columnNames = "email")
})
@NaturalIdCache  // ✅ Natural ID 캐싱 활성화
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @NaturalId   // ✅ email을 Natural ID로 지정
    @Column(name = "email", nullable = false)
    @Embedded
    private Email email;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    // 기타 필드들...
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member member)) return false;
        
        // ✅ Natural ID 기반 동일성 판단
        return Objects.equals(email, member.email);
    }
    
    @Override
    public int hashCode() {
        // ✅ Natural ID 기반 해시코드
        return Objects.hashCode(email);
    }
}
```

### 현재 Repository 구현

```java
@Repository
@RequiredArgsConstructor
public class MemberJpaRepositoryAdapter implements MemberRepository {

    private final MemberJpaRepository jpaRepository;

    @Override
    public Optional<Member> findByEmail(Email email) {
        // ❌ 현재: JPQL 사용 (Natural ID 최적화 미활용)
        return jpaRepository.findByEmail(email);
    }
}

// JpaRepository 구현
public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    
    @Query("select m from Member m join fetch m.roles where m.email = :email")
    Optional<Member> findByEmail(Email email);
}
```

---

## ⚡ 성능 이슈: JPQL vs Natural ID

### 현재 문제점

**JPQL은 기본적으로 2차 캐시를 활용하지 않습니다!** ❌

```java
// 현재 방식의 문제
@Query("select m from Member m join fetch m.roles where m.email = :email")
Optional<Member> findByEmail(Email email);

/*
실제 동작:
1. JPQL 파싱 ✅
2. SQL 변환 ✅  
3. 2차 캐시 확인 ❌ (건너뛰기!)
4. 매번 DB 쿼리 실행 ❌
5. 결과를 캐시에 저장 ✅ (하지만 다음에 활용 안함)
*/
```

### 성능 비교 시뮬레이션

```bash
# 동일한 사용자 로그인 시 (email: user@test.com)

=== JPQL 방식 ===
1차 조회: SELECT m.* FROM member m WHERE m.email = 'user@test.com' (15ms)
2차 조회: SELECT m.* FROM member m WHERE m.email = 'user@test.com' (14ms) 😱
3차 조회: SELECT m.* FROM member m WHERE m.email = 'user@test.com' (15ms) 😱
→ 매번 DB 접근!

=== Natural ID 방식 ===
1차 조회: SELECT m.* FROM member m WHERE m.email = 'user@test.com' (16ms)
2차 조회: (캐시에서 반환) (1ms) 🚀
3차 조회: (캐시에서 반환) (1ms) 🚀
→ 93% 성능 향상!
```

---

## 🚀 Natural ID 최적화 구현

### 1. 최적화된 Repository 추가

```java
// service/member/src/main/java/innercircle/member/infrastructure/adapter/out/OptimizedMemberRepository.java
package innercircle.member.infrastructure.adapter.out;

import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@Slf4j
@Repository
public class OptimizedMemberRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Natural ID를 활용한 최적화된 이메일 조회
     * 2차 캐시 활용으로 성능 대폭 개선
     * 
     * @param email 조회할 이메일
     * @return 회원 엔티티 (캐시 우선 활용)
     */
    public Optional<Member> findByEmailOptimized(Email email) {
        log.debug("🔍 Natural ID로 회원 조회: {}", email.email());
        
        long startTime = System.currentTimeMillis();
        
        Optional<Member> result = entityManager.unwrap(Session.class)
                .byNaturalId(Member.class)
                .with(LockOptions.NONE)  // 캐시 최우선 활용
                .using("email", email)
                .loadOptional();
        
        long duration = System.currentTimeMillis() - startTime;
        
        if (result.isPresent()) {
            log.debug("✅ 회원 조회 성공: {} ({}ms)", email.email(), duration);
        } else {
            log.debug("❌ 회원 조회 실패: {} ({}ms)", email.email(), duration);
        }
        
        return result;
    }
    
    /**
     * 로그인용 최적화 조회
     * Natural ID 캐시 + 연관관계 지연 로딩
     * 
     * @param email 로그인 이메일
     * @return 인증용 회원 정보
     */
    public Optional<Member> findByEmailForAuth(Email email) {
        log.debug("🔐 로그인용 회원 조회: {}", email.email());
        
        // 1차: Natural ID 캐시에서 빠르게 조회
        Optional<Member> member = findByEmailOptimized(email);
        
        if (member.isPresent()) {
            // 2차: 필요 시 연관관계 지연 로딩 (roles는 필요할 때만)
            // member.get().getRoles().size(); // 강제 초기화 (필요한 경우)
            log.debug("🎯 캐시 활용 로그인 조회 완료: {}", email.email());
        }
        
        return member;
    }
    
    /**
     * 상세 정보용 조회 (연관관계 포함)
     * 캐시 활용 후 연관관계 페치
     * 
     * @param email 조회할 이메일
     * @return 연관관계가 로드된 회원 정보
     */
    public Optional<Member> findByEmailWithDetails(Email email) {
        Optional<Member> member = findByEmailOptimized(email);
        
        if (member.isPresent()) {
            // 연관관계 강제 초기화
            member.get().getRoles().size();
            log.debug("📊 상세 정보 조회 완료: {}", email.email());
        }
        
        return member;
    }
    
    /**
     * 하이브리드 조회 방식
     * 캐시 미스 시 JPQL로 한 번에 조회
     * 
     * @param email 조회할 이메일
     * @return 최적화된 조회 결과
     */
    public Optional<Member> findByEmailHybrid(Email email) {
        
        // 1차: Natural ID 캐시 확인
        Optional<Member> cached = findByEmailOptimized(email);
        
        if (cached.isPresent()) {
            log.debug("💾 캐시 히트: {}", email.email());
            return cached;
        }
        
        // 2차: 캐시 미스 시 JPQL로 연관관계까지 한 번에 조회
        log.debug("💿 캐시 미스, JPQL 실행: {}", email.email());
        
        return entityManager.createQuery(
                "select m from Member m join fetch m.roles where m.email = :email", 
                Member.class)
            .setParameter("email", email)
            .getResultList()
            .stream()
            .findFirst();
    }
}
```

### 2. Repository 계층 리팩토링

```java
// 기존 MemberJpaRepositoryAdapter 개선
@Repository
@RequiredArgsConstructor
public class EnhancedMemberRepository implements MemberRepository {
    
    private final MemberJpaRepository jpaRepository;
    private final OptimizedMemberRepository optimizedRepository;
    
    @Override
    public Member save(Member member) {
        return jpaRepository.save(member);
    }
    
    @Override
    public Optional<Member> findByEmail(Email email) {
        // 기본값: 기존 호환성 유지
        return jpaRepository.findByEmail(email);
    }
    
    /**
     * 🚀 로그인 전용 최적화 메서드
     */
    public Optional<Member> findByEmailForLogin(Email email) {
        return optimizedRepository.findByEmailForAuth(email);
    }
    
    /**
     * 📊 상세 조회 전용 메서드
     */
    public Optional<Member> findByEmailWithRoles(Email email) {
        return optimizedRepository.findByEmailWithDetails(email);
    }
}
```

### 3. 서비스 레이어 최적화

```java
@Service
@RequiredArgsConstructor
public class OptimizedAuthApplicationService implements AuthUseCase {
    
    private final EnhancedMemberRepository memberRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenPort tokenPort;
    
    @Override
    public UserAuthInfo findByEmail(String email) {
        
        // 🚀 로그인은 캐시 최적화 버전 사용
        Member member = memberRepository.findByEmailForLogin(new Email(email))
                .orElseThrow(() -> new UserNotExistsException(
                    ErrorCode.LOGIN_FAILED, 
                    "user not found. email=" + email
                ));
        
        if (!member.getStatus().isActive()) {
            throw new IllegalArgumentException("사용자가 비활성화 상태입니다.");
        }
        
        return UserAuthInfo.create(
                member.getId(),
                member.getEmail().email(),
                member.getPassword(),
                member.getRoleNames(),  // 지연 로딩 활용
                member.isActive()
        );
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        
        log.info("🔐 로그인 시도: {}", request.email());
        
        // 캐시 최적화된 사용자 조회
        UserAuthInfo userAuthInfo = findByEmail(request.email());
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), userAuthInfo.password())) {
            log.warn("❌ 비밀번호 불일치: {}", request.email());
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        // JWT 토큰 생성
        String accessToken = tokenPort.generateAccessToken(
                userAuthInfo.userId(),
                userAuthInfo.email(),
                userAuthInfo.roleNames()
        );
        
        String refreshToken = tokenPort.generateRefreshToken(
                userAuthInfo.userId(),
                userAuthInfo.email(),
                userAuthInfo.roleNames()
        );
        
        log.info("✅ 로그인 성공: {} (캐시 활용)", request.email());
        
        return new LoginResponse(accessToken, refreshToken);
    }
}
```

---

## ⚙️ 캐시 설정 최적화

### 1. Hibernate 2차 캐시 설정

```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        # 2차 캐시 활성화
        cache:
          use_second_level_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
          use_query_cache: true
        
        # Natural ID 캐시 최적화
        cache:
          auto_evict_collection_cache: true
          use_structured_cache: true
        
        # 로깅 (개발 시에만)
        generate_statistics: true
        cache:
          use_statistics: true
```

### 2. 캐시 구성 (EhCache 예시)

```xml
<!-- ehcache.xml -->
<config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.ehcache.org/v3">
    
    <!-- Natural ID 캐시 -->
    <cache alias="member_natural_id_cache">
        <key-type>java.lang.String</key-type>
        <value-type>java.lang.Object</value-type>
        <expiry>
            <ttl unit="minutes">30</ttl>  <!-- 30분 캐시 -->
        </expiry>
        <resources>
            <heap unit="entries">1000</heap>  <!-- 최대 1000개 -->
        </resources>
    </cache>
    
    <!-- 엔티티 캐시 -->
    <cache alias="innercircle.member.domain.member.Member">
        <key-type>java.lang.Long</key-type>
        <value-type>innercircle.member.domain.member.Member</value-type>
        <expiry>
            <ttl unit="minutes">30</ttl>
        </expiry>
        <resources>
            <heap unit="entries">1000</heap>
        </resources>
    </cache>
</config>
```

### 3. 캐시 모니터링

```java
@Component
@Slf4j
public class CacheMonitoringService {
    
    @Autowired
    private SessionFactory sessionFactory;
    
    @EventListener
    @Async
    public void handleCacheHit(CacheHitEvent event) {
        log.debug("💾 캐시 히트: {} - 키: {}", 
                 event.getRegionName(), event.getKey());
    }
    
    @EventListener
    @Async
    public void handleCacheMiss(CacheMissEvent event) {
        log.debug("💿 캐시 미스: {} - 키: {}", 
                 event.getRegionName(), event.getKey());
    }
    
    @Scheduled(fixedDelay = 60000) // 1분마다
    public void logCacheStatistics() {
        Statistics stats = sessionFactory.getStatistics();
        
        if (stats.isStatisticsEnabled()) {
            log.info("📊 캐시 통계 - " +
                    "히트율: {}%, " +
                    "히트: {}, " + 
                    "미스: {}, " +
                    "저장: {}",
                    String.format("%.2f", stats.getSecondLevelCacheHitRatio() * 100),
                    stats.getSecondLevelCacheHitCount(),
                    stats.getSecondLevelCacheMissCount(),
                    stats.getSecondLevelCachePutCount()
            );
        }
    }
}
```

---

## 🧪 성능 테스트

### 벤치마크 테스트

```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class NaturalIdPerformanceTest {
    
    @Autowired
    private MemberJpaRepository jpqlRepository;
    
    @Autowired
    private OptimizedMemberRepository naturalIdRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    private static final String TEST_EMAIL = "performance@test.com";
    private static final int ITERATION_COUNT = 100;
    
    @Test
    @Order(1)
    void 데이터_준비() {
        Member member = Member.create(TEST_EMAIL, "성능테스트", "password", "1990-01-01", "MALE");
        jpqlRepository.save(member);
        entityManager.flush();
        entityManager.clear();
        log.info("✅ 테스트 데이터 준비 완료");
    }
    
    @Test
    @Order(2)
    void JPQL_성능_테스트() {
        
        long totalTime = 0;
        
        for (int i = 0; i < ITERATION_COUNT; i++) {
            entityManager.clear(); // 1차 캐시 클리어
            
            long startTime = System.currentTimeMillis();
            
            Optional<Member> result = jpqlRepository.findByEmail(new Email(TEST_EMAIL));
            
            long duration = System.currentTimeMillis() - startTime;
            totalTime += duration;
            
            assertThat(result).isPresent();
        }
        
        double averageTime = (double) totalTime / ITERATION_COUNT;
        log.info("📊 JPQL 평균 응답시간: {}ms (총 {}회)", averageTime, ITERATION_COUNT);
    }
    
    @Test
    @Order(3)
    void NaturalId_성능_테스트() {
        
        long totalTime = 0;
        
        for (int i = 0; i < ITERATION_COUNT; i++) {
            entityManager.clear(); // 1차 캐시 클리어
            
            long startTime = System.currentTimeMillis();
            
            Optional<Member> result = naturalIdRepository.findByEmailOptimized(new Email(TEST_EMAIL));
            
            long duration = System.currentTimeMillis() - startTime;
            totalTime += duration;
            
            assertThat(result).isPresent();
        }
        
        double averageTime = (double) totalTime / ITERATION_COUNT;
        log.info("🚀 Natural ID 평균 응답시간: {}ms (총 {}회)", averageTime, ITERATION_COUNT);
    }
    
    @Test
    @Order(4)
    void 캐시_히트율_확인() {
        
        Statistics stats = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class)
                .getStatistics();
        
        if (stats.isStatisticsEnabled()) {
            double hitRatio = stats.getSecondLevelCacheHitRatio() * 100;
            
            log.info("📈 2차 캐시 통계:");
            log.info("  - 히트율: {}%", String.format("%.2f", hitRatio));
            log.info("  - 캐시 히트: {}", stats.getSecondLevelCacheHitCount());
            log.info("  - 캐시 미스: {}", stats.getSecondLevelCacheMissCount());
            log.info("  - 캐시 저장: {}", stats.getSecondLevelCachePutCount());
            
            assertThat(hitRatio).isGreaterThan(80.0); // 80% 이상 히트율 기대
        }
    }
}
```

### 예상 성능 결과

```bash
# 예상 벤치마크 결과

📊 JPQL 평균 응답시간: 12.5ms (총 100회)
🚀 Natural ID 평균 응답시간: 1.2ms (총 100회)

📈 성능 개선:
  - 응답시간: 90.4% 향상 (12.5ms → 1.2ms)
  - 처리량: 10.4배 증가

📈 2차 캐시 통계:
  - 히트율: 92.5%
  - 캐시 히트: 185
  - 캐시 미스: 15
  - 캐시 저장: 15
```

---

## ⚠️ 주의사항 및 제약

### 1. Natural ID 변경 시 주의

```java
// ❌ Natural ID 변경 시 캐시 문제 발생 가능
public void changeEmail(String newEmail) {
    this.email = new Email(newEmail);  // 캐시 불일치 위험!
}

// ✅ 올바른 Natural ID 변경 방법
@Transactional
public void changeEmailSafely(String newEmail) {
    
    // 기존 캐시 무효화
    SessionFactory sessionFactory = entityManager.getEntityManagerFactory()
            .unwrap(SessionFactory.class);
    
    sessionFactory.getCache().evictNaturalIdData(Member.class, this.email);
    
    // 이메일 변경
    this.email = new Email(newEmail);
    
    log.warn("⚠️ 이메일 변경으로 인한 캐시 무효화: {} → {}", 
             this.email, newEmail);
}
```

### 2. 복합 Natural ID

```java
// 여러 필드 조합도 가능
@Entity
public class OrderItem {
    @NaturalId
    private Long orderId;
    
    @NaturalId  
    private Long productId;
    
    // orderId + productId 조합이 Natural ID
}

// 복합 Natural ID 조회
session.byNaturalId(OrderItem.class)
       .using("orderId", 123L)
       .using("productId", 456L)
       .load();
```

### 3. 성능 모니터링 필수

```java
// 캐시 히트율이 낮은 경우 확인사항
@Component
public class CacheHealthChecker {
    
    @Scheduled(fixedDelay = 300000) // 5분마다
    public void checkCacheHealth() {
        Statistics stats = sessionFactory.getStatistics();
        
        double hitRatio = stats.getSecondLevelCacheHitRatio() * 100;
        
        if (hitRatio < 70.0) {
            log.warn("⚠️ 캐시 히트율 저조: {}% (70% 미만)", 
                    String.format("%.2f", hitRatio));
            
            // 알림 발송 또는 캐시 설정 검토 필요
        }
    }
}
```

---

## 📊 적용 전후 성능 비교

### 기존 방식 (JPQL)

```bash
# 로그인 요청 처리 (동일 사용자 10회)
요청 1: DB 쿼리 (15ms)
요청 2: DB 쿼리 (14ms)  
요청 3: DB 쿼리 (15ms)
...
요청 10: DB 쿼리 (14ms)

총 소요시간: 145ms
평균 응답시간: 14.5ms
DB 쿼리 횟수: 10회
```

### 최적화 방식 (Natural ID + Cache)

```bash
# 로그인 요청 처리 (동일 사용자 10회)
요청 1: DB 쿼리 (16ms) + 캐시 저장
요청 2: 캐시 반환 (1ms)
요청 3: 캐시 반환 (1ms)
...
요청 10: 캐시 반환 (1ms)

총 소요시간: 25ms
평균 응답시간: 2.5ms
DB 쿼리 횟수: 1회

성능 향상: 82.8% (145ms → 25ms)
DB 부하 감소: 90% (10회 → 1회)
```

---

## 🚀 마이그레이션 가이드

### 1단계: 최적화 Repository 추가

```bash
# 새 파일 생성
service/member/src/main/java/innercircle/member/infrastructure/adapter/out/OptimizedMemberRepository.java
```

### 2단계: 기존 Repository 확장

```java
// 기존 MemberJpaRepositoryAdapter에 메서드 추가
public Optional<Member> findByEmailCached(Email email) {
    return optimizedRepository.findByEmailOptimized(email);
}
```

### 3단계: 서비스 레이어에서 선택적 사용

```java
// 로그인 시에만 캐시 최적화 적용
@Override
public UserAuthInfo findByEmail(String email) {
    // 기존: return memberRepository.findByEmail(new Email(email))...
    // 변경: return memberRepository.findByEmailCached(new Email(email))...
}
```

### 4단계: 캐시 설정 활성화

```yaml
# application.yml에 캐시 설정 추가
spring.jpa.properties.hibernate.cache.use_second_level_cache: true
```

### 5단계: 성능 모니터링

```java
// 캐시 통계 로깅 활성화
spring.jpa.properties.hibernate.generate_statistics: true
```

---

## 📝 체크리스트

### 구현 완료 확인

- [ ] `@NaturalId` 어노테이션 적용 확인
- [ ] `@NaturalIdCache` 어노테이션 추가
- [ ] OptimizedMemberRepository 구현
- [ ] 캐시 설정 활성화 (application.yml)
- [ ] 성능 테스트 작성 및 실행
- [ ] 캐시 모니터링 구현
- [ ] 로그인 서비스에 최적화 적용

### 성능 검증

- [ ] 캐시 히트율 70% 이상 달성
- [ ] 평균 응답시간 80% 이상 개선
- [ ] DB 쿼리 횟수 90% 이상 감소
- [ ] 동시 접속 부하 테스트 통과

---

## 📚 참고 자료

### Hibernate 공식 문서
- [Natural ID Mapping](https://docs.jboss.org/hibernate/orm/6.4/userguide/html_single/Hibernate_User_Guide.html#naturalid)
- [Second Level Cache](https://docs.jboss.org/hibernate/orm/6.4/userguide/html_single/Hibernate_User_Guide.html#caching)

### 성능 최적화 가이드
- [JPA Performance Best Practices](https://thoughts-on-java.org/jpa-performance-optimization/)
- [Hibernate Caching Strategies](https://vladmihalcea.com/how-to-boost-the-performance-of-your-spring-data-jpa-application/)

### 관련 프로젝트 문서
- [Member Domain 설계](./member-domain-design.md)
- [JWT 구현 가이드](./JWT_IMPLEMENTATION_GUIDE.md)

---

**최종 업데이트**: 2025-08-19  
**작성자**: Commerce 개발팀  
**버전**: 1.0.0

---

> 💡 **팁**: Natural ID 최적화는 로그인과 같이 **동일한 조건으로 반복 조회가 많은 경우**에 특히 효과적입니다. 단발성 조회나 복잡한 조건 검색에는 기존 JPQL을 그대로 사용하는 것이 좋습니다.
