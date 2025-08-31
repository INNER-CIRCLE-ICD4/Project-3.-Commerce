package innercircle.member.application.service;

import innercircle.member.application.port.out.MemberCommandPort;
import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberDomainService;
import innercircle.member.domain.member.SnowFlakeGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateMemberServiceTest {

    @Mock
    private MemberCommandPort memberCommandPort;

    @Mock
    private MemberDomainService memberDomainService;

    @InjectMocks
    private MemberApplicationService memberApplicationService;

    @Test
    void 회원_가입_신청_성공() {

        Member member = Member.create("asdz453@gmail.com", "노성웅", "12345678a", "1996-04-23", "MAIL");

        Member encodedMember = member.withEncodedPassword("encodedPassword1234");

        long id = SnowFlakeGenerator.GENERATOR.nextId();
        ReflectionTestUtils.setField(member, "id", id);

        when(memberDomainService.existsByEmail("asdz453@gmail.com"))
                .thenReturn(true);

        when(memberDomainService.encodePassword(any()))
                .thenReturn("encodedPassword1234");

        when(memberCommandPort.save(any(Member.class)))
                .thenReturn(encodedMember);

        Member response = memberApplicationService.createMember(encodedMember);

        assertThat(response.getEmail().email()).isEqualTo("asdz453@gmail.com");
        assertThat(response.getName()).isEqualTo(member.getName());
        assertThat(response.getPassword()).isEqualTo(encodedMember.getPassword());

        verify(memberCommandPort).save(any(Member.class));
    }

}