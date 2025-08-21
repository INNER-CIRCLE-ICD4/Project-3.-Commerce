package innercircle.member.application.port.out;

import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;

import java.util.Optional;

public interface MemberQueryRepository {
    Optional<Member> findByEmailUsingNaturalId(Email email);
}
