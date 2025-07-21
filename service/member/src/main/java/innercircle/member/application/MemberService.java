package innercircle.member.application;


import innercircle.member.domain.Member;
import innercircle.member.domain.MemberDomainService;
import innercircle.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements MemberUseCase {

    private final MemberRepository memberRepository;
    private final MemberDomainService memberDomainService;

    @Override
    public MemberResponse createMember(MemberCreateRequest request) {

        memberDomainService.existsByEmail(request.email(), memberRepository);

        Member member = Member.create(
                request.email(),
                request.name(),
                request.password(),
                request.birthDate(),
                request.gender()
        );

        Member save = memberRepository.save(member);

        return MemberResponse.from(save);
    }
}
