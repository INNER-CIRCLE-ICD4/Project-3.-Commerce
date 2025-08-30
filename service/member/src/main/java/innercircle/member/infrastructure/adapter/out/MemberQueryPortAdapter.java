package innercircle.member.infrastructure.adapter.out;

import innercircle.member.application.port.out.MemberQueryPort;
import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberStatus;
import innercircle.member.domain.member.RoleType;
import innercircle.member.infrastructure.persistence.MemberJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public Page<Member> searchMembers(
            String keyword,
            String name,
            String email,
            MemberStatus status,
            RoleType role,
            Pageable page) {

        // 회원 검색
        Page<Member> members = jpaRepository.searchMembers(keyword, name, email, status, role, page);

        if (members.getContent().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), page, 0);
        }

        //  배치 크기 제한 (안전장치)
        List<Long> memberIds = members.getContent().stream()
                .map(Member::getId)
                .limit(100)  // 최대 100개로 제한
                .toList();

        // 회원 권한과 함께 조회 (N+1 방지)
        List<Member> membersWithRoles = jpaRepository.findMembersWithRoles(memberIds);


        // 회원 중복 제거
        Map<Long, Member> memberMap = membersWithRoles.stream()
                .collect(Collectors.toMap(
                        Member::getId,
                        Function.identity(),
                        (existing, replacement) -> {
                            log.warn("⚠️ 중복 Member ID: {} (기존값 유지)", existing.getId());
                            return existing;
                        }, LinkedHashMap::new
                ));


        List<Long> dupleIds = members.stream()
                .map(Member::getId)
                .filter(id -> !memberMap.containsKey(id))
                .toList();

        // 누락 데이터 발생 시 일단 알려주기
        if (!dupleIds.isEmpty()) {
            log.error("🚨 Member 중복 데이터 발견: {}", dupleIds);
        }

        return new PageImpl<>(membersWithRoles, page, members.getTotalElements());
    }
}
