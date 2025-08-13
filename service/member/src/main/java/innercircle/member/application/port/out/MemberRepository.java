package innercircle.member.application.port.out;

import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;

import java.util.Optional;

public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findByEmail(Email email);

}
