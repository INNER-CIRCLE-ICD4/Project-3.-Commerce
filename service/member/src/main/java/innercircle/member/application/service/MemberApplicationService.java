package innercircle.member.application.service;


import innercircle.member.application.MemberCreateRequest;
import innercircle.member.application.MemberResponse;
import innercircle.member.application.port.in.MemberUseCase;
import innercircle.member.application.port.out.PasswordEncoderPort;
import innercircle.member.domain.Member;
import innercircle.member.domain.MemberDomainService;
import innercircle.member.application.port.out.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberApplicationService implements MemberUseCase {

    private final MemberRepository memberRepository;
    private final MemberDomainService memberDomainService;
    private final PasswordEncoderPort passwordEncoderPort;


    @Override
    public MemberResponse createMember(MemberCreateRequest request) {

        memberDomainService.existsByEmail(request.email(), memberRepository);

        Member member = Member.create(
                request.email(),
                request.name(),
                passwordEncoderPort.encode(request.password()),
                request.birthDate(),
                request.gender()
        );

        Member save = memberRepository.save(member);

        return MemberResponse.from(save);
    }
}
