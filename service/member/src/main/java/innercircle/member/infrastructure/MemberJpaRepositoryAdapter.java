package innercircle.member.infrastructure;

import innercircle.member.domain.Email;
import innercircle.member.domain.Member;
import innercircle.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepositoryAdapter implements MemberRepository {

    private final MemberJpaRepository jpaRepository;


    @Override
    public Member save(Member member) {

        return jpaRepository.save(member);
    }

    @Override
    public Optional<Member> findByEmail(Email email) {

        return jpaRepository.findByEmail(email);
    }
}
