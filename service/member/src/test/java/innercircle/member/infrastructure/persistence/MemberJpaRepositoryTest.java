package innercircle.member.infrastructure.persistence;

import innercircle.member.domain.member.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class MemberJpaRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    private Member savedMember1;
    private Member savedMember2;
    private Member savedMember3;
    private Member savedMember4;


    @BeforeEach
    void setup() {
        // 🧪 테스트 데이터 저장
        savedMember1 = createAndSaveMember("test1@test.com", "김철수", MemberStatus.ACTIVE);
        savedMember2 = createAndSaveMember("test2@test.com", "이영희", MemberStatus.ACTIVE);
        savedMember3 = createAndSaveMember("admin@test.com", "관리자", MemberStatus.ACTIVE);

        // Admin 역할 추가

        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트 초기화
    }


    @Test
    @DisplayName("키워드 검색 - 이름 부분 매칭")
    void 키워드검색() {

        String keyword = "김";
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Member> members = memberJpaRepository.searchMembers(keyword, null, null, null, null, pageRequest);

        assertThat(members.getTotalElements()).isEqualTo(3);
        assertThat(members.getContent().get(0).getName()).isEqualTo("김철수");
        assertThat(members.getContent().get(0).getRoleNames()).isNotEqualTo("김철수");

    }

    @Test
    @DisplayName("회원 검색(권한 포함) - 회원 id 목록으로 조회")
    void findMembersWithRole() {

        List<Long> ids = List.of(savedMember1.getId(), savedMember2.getId(), savedMember3.getId());

        List<Member> membersWithRoles = memberJpaRepository.findMembersWithRoles(ids);

        assertThat(membersWithRoles.size()).isEqualTo(3);
        assertThat(membersWithRoles.get(0).getName()).isEqualTo("관리자");
        assertThat(membersWithRoles.get(1).getName()).isEqualTo("이영희");
        assertThat(membersWithRoles.get(2).getName()).isEqualTo("김철수");
    }

    @Test
    @DisplayName("조건 검색 및 배치 수정 테스트")
    void membersWithoutRole() {
        Page<Member> members = memberJpaRepository.searchMembers("김", null, null, null, null, PageRequest.of(0, 10));
        List<Long> ids = members.stream().map(Member::getId).toList();

        List<Member> membersWithRoles = memberJpaRepository.findMembersWithRoles(ids);

        assertThat(membersWithRoles.size()).isEqualTo(3);
        assertThat(membersWithRoles.get(0).getName()).isEqualTo("김철수");
    }


    @BeforeEach
    void setUp() {

        setupRealWorldTestData();
        createLargeMemberDataSet(100);

        // Hibernate 통계 초기화
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();

        // 통계 초기화
        statistics.clear();
        statistics.setStatisticsEnabled(true);
    }


    @Test
    void naturalIdCache_성능비교테스트() {
        Member member = Member.create(
                "sw.noh@gmail.com",
                "노성웅",
                "password1234",
                "2025-07-21",
                "MALE"
        );


        Member save = memberJpaRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        Email email = member.getEmail();

        // 첫 번쨰 조회 (캐시 미사용)
        long start1 = System.nanoTime();
        var result1 = entityManager.unwrap(Session.class)
                .byNaturalId(Member.class)
                .using("email", email)
                .load();
        long time1 = System.nanoTime() - start1;

        System.out.println("🚀 첫 번째 조회 시간: " + time1 + "ns");

        assertThat(result1.getEmail().email()).isEqualTo(save.getEmail().email());

        // 두 번째 조회 (캐시 사용)
        long start2 = System.nanoTime();
        var result2 = entityManager.unwrap(Session.class)
                .byNaturalId(Member.class)
                .using("email", email)
                .load();
        long time2 = System.nanoTime() - start2;
        System.out.println("🚀 두 번째 조회 시간: " + time2 + "ns");

        assertThat(result2.getEmail().email()).isEqualTo(save.getEmail().email());

        System.out.printf("🚀 성능 향상: %.1f배%n", (double) time1 / time2);
    }


    @Test
    @DisplayName("회원 검색 기능")
    void searchMembers() {

        Page<Member> members = memberJpaRepository.searchMembers(null, null, null, null, RoleType.ADMIN, PageRequest.of(0, 10));


        assertThat(members).hasSize(1);
        assertThat(members.getContent().getFirst().getName()).isEqualTo("관리자");

    }

    @Test
    @DisplayName("회원에 권한을 중복으로 부여할 수 없다.")
    void doNotGrantDupleRole() {

        ;

        assertThatThrownBy(() -> MemberRole.grantAdminRole(savedMember4))
                .isInstanceOf(DuplicateRoleException.class);

    }

    private void setupRealWorldTestData() {

        savedMember1 = createMemberWithRole("kim@gmail.com", "김철수", MemberStatus.ACTIVE);
        savedMember2 = createMemberWithRole("jjb@gmail.com", "김영희", MemberStatus.ACTIVE);
        savedMember3 = createMemberWithRole("park@gmail.com", "박민수", MemberStatus.ACTIVE);
        savedMember4 = createMemberWithRole("swnoh@gmail.com", "관리자", MemberStatus.INACTIVE);

        MemberRole.grantAdminRole(savedMember4);
        MemberRole.grantSellerRole(savedMember3);
    }


    private Member createMemberWithRole(String email, String name, MemberStatus status) {

        Member member = Member.create(email, name, "password1234", "1996-04-23", Gender.MALE.name());
        member.changeStatus(status);

        Member savedMember = testEntityManager.persistAndFlush(member);
        return testEntityManager.merge(savedMember);
    }


    private void createLargeMemberDataSet(int count) {

        for (int i = 0; i <= count; i++) {
            createMemberWithRole(
                    "bulk" + i + "@test.com",
                    "대량데이터" + i,
                    MemberStatus.ACTIVE
            );
        }
    }

    private Member createSimpleMember(String email, String name, MemberStatus status) {
        Member member = Member.create(email, name, "password1234", "1996-04-23", "MALE");
        member.changeStatus(status);
        return testEntityManager.persistAndFlush(member);
    }


    // 테스트 헬퍼 메서드들...
    private Member createAndSaveMember(String email, String name, MemberStatus status) {

        Member member = Member.create(email, name, "password123", "1990-01-01", "MALE");
        setField(member, "status", status);
        return testEntityManager.persistAndFlush(member);
    }


    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            try {
                var field = target.getClass().getSuperclass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception ex) {
                throw new RuntimeException("테스트 필드 설정 실패: " + fieldName, ex);
            }
        }
    }
}