package innercircle.member.application.service;


import innercircle.member.application.port.in.MemberUseCase;
import innercircle.member.application.port.out.MemberCommandPort;
import innercircle.member.application.port.out.MemberQueryPort;
import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberDomainService;
import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberApplicationService implements MemberUseCase {

    private final MemberCommandPort memberCommandPort;
    private final MemberQueryPort memberQueryPort;
    private final MemberDomainService memberDomainService;

    @Override
    @Transactional
    public Member createMember(Member member) {

        memberDomainService.existsByEmail(member.getEmail().email());

        Member saveMember = member.withEncodedPassword(
                memberDomainService.encodePassword(member.getPassword())
        );

        return memberCommandPort.save(saveMember);
    }

    @Override
    public Page<Member> searchMembers(MemberSearchRequest request) {

        return  memberQueryPort.searchMembers(
                request.keyword(),
                request.name(),
                request.email(),
                request.memberStatus(),
                request.role(),
                PageRequest.of(request.page(), request.size())
        );
    }
}
