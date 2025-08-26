package innercircle.member.application.port.in;

import innercircle.member.domain.member.Member;

public interface MemberUseCase {

    Member createMember(Member member);

}