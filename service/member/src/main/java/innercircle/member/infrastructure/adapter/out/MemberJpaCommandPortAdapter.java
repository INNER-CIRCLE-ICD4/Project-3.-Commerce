package innercircle.member.infrastructure.adapter.out;

import innercircle.member.application.port.out.MemberCommandPort;
import innercircle.member.domain.member.Member;
import innercircle.member.infrastructure.persistence.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberJpaCommandPortAdapter implements MemberCommandPort {

    private final MemberJpaRepository jpaRepository;

    @Override
    @Transactional
    public Member save(Member member) {
        return jpaRepository.save(member);
    }
}
