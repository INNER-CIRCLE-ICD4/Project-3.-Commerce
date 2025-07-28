package innercircle.member.application;

import innercircle.member.application.port.out.PasswordEncoderPort;
import innercircle.member.application.service.MemberApplicationService;
import innercircle.member.domain.Member;
import innercircle.member.domain.MemberDomainService;
import innercircle.member.application.port.out.MemberRepository;
import innercircle.member.domain.SnowFlakeGenerator;
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
    private MemberRepository memberRepository;

    @Mock
    private MemberDomainService memberDomainService;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private MemberApplicationService memberApplicationService;

    @Test
    void 회원_가입_신청_성공() {

        MemberCreateRequest memberCreateRequest = new MemberCreateRequest("asdz453@gmail.com", "노성웅", "12345678a", "1996-04-23", "MAIL");

        Member member = Member.create("asdz453@gmail.com", "노성웅", "12345678a", "1996-04-23", "MAIL");

        long id = SnowFlakeGenerator.GENERATOR.nextId();
        ReflectionTestUtils.setField(member, "id", id);


        when(passwordEncoderPort.encode("12345678a")).thenReturn("$2a$12$xlARSI2aAoLcFVWJiMoN..XUvDhME0nXYbaMO2UTaoTT6835QhMcu");
        when(memberDomainService.existsByEmail(memberCreateRequest.email(), memberRepository)).thenReturn(true);
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        MemberResponse response = memberApplicationService.createMember(memberCreateRequest);

        assertThat(response.memberId()).isEqualTo(id);
        assertThat(response.name()).isEqualTo(member.getName());

        verify(memberDomainService).existsByEmail(memberCreateRequest.email(), memberRepository);
        verify(memberRepository).save(any(Member.class));
    }

}