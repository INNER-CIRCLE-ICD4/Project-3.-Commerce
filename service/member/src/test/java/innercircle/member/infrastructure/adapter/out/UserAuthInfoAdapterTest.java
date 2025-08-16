package innercircle.member.infrastructure.adapter.out;

import innercircle.member.application.port.out.MemberRepository;
import innercircle.member.application.port.out.UserAuthInfoProvider;
import innercircle.member.domain.auth.UserAuthInfo;
import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthInfoAdapterTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    UserAuthInfoAdapter userAuthInfoProvider;

    @Test
    @DisplayName("findByEmail 메서드 호출 테스트")
    void findByEmailTest() {

        when(memberRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(Member.create("asdz453@gmail.com", "노성웅", "password1234", "2025-07-21", "MAIL")));

        UserAuthInfo byEmail = userAuthInfoProvider.findByEmail("asdz453@gmail.com");

        assertThat(byEmail.getEmail()).isEqualTo("asdz453@gmail.com");
        assertThat(byEmail.isActive()).isTrue();
    }


    @Test
    @DisplayName("findByEmail 메서드 호출 테스트")
    void findByEmailTest_notActive() {

        Member member = Member.create("asdz453@gmail.com", "노성웅", "password1234", "2025-07-21", "MAIL");
        member.inActivate();

        OngoingStubbing<Optional<Member>> optionalOngoingStubbing = when(memberRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> userAuthInfoProvider.findByEmail("asdz453@gmail.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자가 비활성화 상태입니다. user_status=");

    }



}