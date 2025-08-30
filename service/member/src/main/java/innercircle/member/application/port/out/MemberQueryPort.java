package innercircle.member.application.port.out;

import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberStatus;
import innercircle.member.domain.member.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberQueryPort {

    Optional<Member> findByEmail(Email email);

    Optional<Member> findByEmailUsingNaturalId(Email email);

    Page<Member> searchMembers(String keyword, String name, String email, MemberStatus status, RoleType role, Pageable page);
}
