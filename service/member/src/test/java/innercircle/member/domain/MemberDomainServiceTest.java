package innercircle.member.domain;

import innercircle.member.application.port.out.MemberQueryPort;
import innercircle.member.application.port.out.MemberCommandPort;
import innercircle.member.domain.member.DuplicateRequestException;
import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberDomainService;
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
    private MemberCommandPort memberCommandPort;

    @Mock
    private MemberQueryPort memberQueryPort;

    @InjectMocks
    private MemberDomainService memberDomainService;


    @Test
    void 이메일_중복_검증_성공() {

        Email email = new Email("asdz453@gmail.com");

        when(memberQueryPort.findByEmailUsingNaturalId(email)).thenReturn(Optional.empty());

        assertThatCode(() -> memberDomainService.existsByEmail("asdz453@gmail.com")).doesNotThrowAnyException();

        verify(memberQueryPort).findByEmailUsingNaturalId(email);
    }

    @Test
    void 이메일_중복_검증_실패() {
        String mail = "asdz453@gmail.com";

        when(memberQueryPort.findByEmailUsingNaturalId(any(Email.class))).thenReturn(Optional.of(Member.create("asdz453@gmail.com", "노성웅", "password1234", "2025-07-21", "MALE")));

        assertThatThrownBy(() -> memberDomainService.existsByEmail(mail))
                .isInstanceOf(DuplicateRequestException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다.");

        verify(memberQueryPort).findByEmailUsingNaturalId(new Email(mail));

    }


}