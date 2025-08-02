package innercircle.member.infrastructure.persistence;

import innercircle.member.domain.Email;
import innercircle.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(Email email);
}
