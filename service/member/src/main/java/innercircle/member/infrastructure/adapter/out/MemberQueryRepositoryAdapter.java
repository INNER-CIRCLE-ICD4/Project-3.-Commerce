package innercircle.member.infrastructure.adapter.out;

import innercircle.member.application.port.out.MemberQueryRepository;
import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Transactional(readOnly = true)
@Repository
public class MemberQueryRepositoryAdapter implements MemberQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Member> findByEmailUsingNaturalId(Email email) {

        return entityManager.unwrap(Session.class)
                .byNaturalId(Member.class)
                .using("email", email)
                .loadOptional();
    }
}
