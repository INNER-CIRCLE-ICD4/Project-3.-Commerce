package innercircle.member.application.port.in;

import innercircle.member.application.MemberCreateRequest;
import innercircle.member.application.MemberCreateResponse;
import innercircle.member.domain.member.Member;

public interface MemberUseCase {

    Member createMember(Member member);

}