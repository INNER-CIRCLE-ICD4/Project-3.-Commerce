package innercircle.member.application.port.out;

import innercircle.member.domain.member.Member;

public interface MemberRepository {

    Member save(Member member);

}
