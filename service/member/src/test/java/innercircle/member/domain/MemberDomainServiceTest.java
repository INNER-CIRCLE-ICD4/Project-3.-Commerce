package innercircle.member.domain;

import com.sun.jdi.request.DuplicateRequestException;
import innercircle.member.application.port.out.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberDomainServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberDomainService memberDomainService;


    @Test
    void 이메일_중복_검증_성공() {

        Email email = new Email("asdz453@gmail.com");

        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatCode(() -> memberDomainService.existsByEmail("asdz453@gmail.com", memberRepository))
                .doesNotThrowAnyException();

        verify(memberRepository).findByEmail(email);
    }

    @Test
    void 이메일_중복_검증_실패() {
        String mail = "asdz453@gmail.com";

        when(memberRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(Member.create("asdz453@gmail.com", "노성웅", "password1234", "2025-07-21", "MAIL")));

        assertThatThrownBy(() -> memberDomainService.existsByEmail(mail, memberRepository))
                .isInstanceOf(DuplicateRequestException.class)
                .hasMessageContaining("이미 가입된 이메일입니다. email");

        verify(memberRepository).findByEmail(new Email(mail));

    }


}