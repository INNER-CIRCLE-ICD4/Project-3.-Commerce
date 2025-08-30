package innercircle.member.application.port.out;

import innercircle.member.domain.member.Member;

public interface MemberCommandPort {

    Member save(Member member);

}
