package innercircle.member.application.service;


import innercircle.member.application.port.in.MemberUseCase;
import innercircle.member.application.port.out.PasswordEncoderPort;
import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberDomainService;
import innercircle.member.application.port.out.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberApplicationService implements MemberUseCase {

    private final MemberRepository memberRepository;
    private final MemberDomainService memberDomainService;

    @Override
    @Transactional
    public Member createMember(Member member) {

        memberDomainService.existsByEmail(member.getEmail().email());

        Member saveMember = member.withEncodedPassword(
                memberDomainService.encodePassword(member.getPassword())
        );

        return memberRepository.save(saveMember);
    }
}
