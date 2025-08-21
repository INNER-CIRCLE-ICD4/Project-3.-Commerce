package innercircle.member.infrastructure.adapter.out;

import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import innercircle.member.application.port.out.MemberRepository;
import innercircle.member.infrastructure.persistence.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberJpaRepositoryAdapter implements MemberRepository {

    private final MemberJpaRepository jpaRepository;


    @Override
    @Transactional
    public Member save(Member member) {

        return jpaRepository.save(member);
    }

    @Override
    public Optional<Member> findByEmail(Email email) {

        return jpaRepository.findByEmail(email);
    }
}
