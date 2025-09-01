package innercircle.member.infrastructure.adapter.out;

import innercircle.member.domain.member.*;
import innercircle.member.infrastructure.persistence.MemberJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberQueryPortAdapterTest {

    @InjectMocks
    private MemberQueryPortAdapter memberQueryPort;

    @Mock
    private MemberJpaRepository jpaRepository;


    Member savedMember1;
    Member savedMember2;
    Member savedMember3;
    Member savedMember4;

    @BeforeEach
    public void setup() {
        setupRealWorldTestData();
    }


    @Test
    @DisplayName("회원 조회 쿼리")
    void searchMember() {

        List<Member> memberList = List.of(
                savedMember1, savedMember2, savedMember3, savedMember4
        );

        Page<Member> members = new PageImpl<>(memberList, PageRequest.of(0, 10), 3);

        when(jpaRepository.searchMembers(any(), any(), any(), any(), any(), any())).thenReturn(members);
        when(jpaRepository.findMembersWithRoles(any())).thenReturn(memberList);

        Page<Member> result = memberQueryPort.searchMembers(null, null, null, null, null, members.getPageable());

        verify(jpaRepository).searchMembers(any(), any(), any(), any(), any(), any());
        assertThat(result).hasSize(4);

    }

    private void setupRealWorldTestData() {

        savedMember1 = createMemberWithRole("kim@gmail.com", "김철수", MemberStatus.ACTIVE);
        long id = SnowFlakeGenerator.GENERATOR.nextId();
        ReflectionTestUtils.setField(savedMember1, "id", id);
        savedMember2 = createMemberWithRole("jjb@gmail.com", "김영희", MemberStatus.ACTIVE);
        ReflectionTestUtils.setField(savedMember2, "id", id);
        savedMember3 = createMemberWithRole("park@gmail.com", "박민수", MemberStatus.ACTIVE);
        ReflectionTestUtils.setField(savedMember3, "id", id);
        savedMember4 = createMemberWithRole("swnoh@gmail.com", "관리자", MemberStatus.INACTIVE);

        MemberRole.grantAdminRole(savedMember4);
        MemberRole.grantSellerRole(savedMember3);
    }

    private Member createMemberWithRole(String email, String name, MemberStatus status) {
        Member member = Member.create(email, name, "password1234", "1996-04-23", Gender.MALE.name());
        member.changeStatus(status);
        return member;

    }
}