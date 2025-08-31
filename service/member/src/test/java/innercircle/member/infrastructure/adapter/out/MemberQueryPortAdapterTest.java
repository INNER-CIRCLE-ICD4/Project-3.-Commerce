package innercircle.member.infrastructure.adapter.out;

import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import innercircle.member.infrastructure.persistence.MemberJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MemberQueryPortAdapterTest {


    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
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
                "MAIL"
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

}