package innercircle.member.application.port.in;

import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberRole;
import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberSearchRequest;
import org.springframework.data.domain.Page;

public interface MemberUseCase {

    Member createMember(Member member);

    Page<Member> searchMembers(MemberSearchRequest request);

    Member findByMemberId(Long memberId);

    MemberRole grantAdminRole(Long memberId);

    MemberRole grantSellerRole(Long memberId);

}