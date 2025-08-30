package innercircle.member.infrastructure.persistence;

import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberStatus;
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

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MemberJpaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    private Member savedMember1;
    private Member savedMember2;
    private Member savedMember3;


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

        assertThat(members.getTotalElements()).isEqualTo(1);
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

        assertThat(membersWithRoles.size()).isEqualTo(1);
        assertThat(membersWithRoles.get(0).getName()).isEqualTo("김철수");
    }

    // 테스트 헬퍼 메서드들...
    private Member createAndSaveMember(String email, String name, MemberStatus status) {

        Member member = Member.create(email, name, "password123", "1990-01-01", "MALE");
        setField(member, "status", status);
        return entityManager.persistAndFlush(member);
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