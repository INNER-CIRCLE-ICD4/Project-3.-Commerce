package innercircle.member.infrastructure.adapter.out;

import innercircle.member.application.port.out.MemberQueryPort;
import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import innercircle.member.infrastructure.persistence.MemberJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Transactional(readOnly = true)
@Repository
@RequiredArgsConstructor
public class MemberQueryPortAdapter implements MemberQueryPort {

    @PersistenceContext
    private EntityManager entityManager;

    private final MemberJpaRepository jpaRepository;

    @Override
    public Optional<Member> findByEmail(Email email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public Optional<Member> findByEmailUsingNaturalId(Email email) {

        return entityManager.unwrap(Session.class)
                .byNaturalId(Member.class)
                .using("email", email)
                .loadOptional();
    }
}
