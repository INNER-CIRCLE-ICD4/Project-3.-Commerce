package innercircle.member.infrastructure.persistence;

import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    @Query("select m from Member m join fetch m.roles where m.email = :email")
    Optional<Member> findByEmail(Email email);
}
