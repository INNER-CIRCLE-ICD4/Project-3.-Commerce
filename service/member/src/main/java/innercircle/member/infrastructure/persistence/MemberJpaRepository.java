package innercircle.member.infrastructure.persistence;

import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberStatus;
import innercircle.member.domain.member.RoleType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    @Query("select m from Member m join fetch m.roles where m.email = :email")
    Optional<Member> findByEmail(Email email);


    @Query(value = """
            SELECT DISTINCT m FROM Member m
            LEFT JOIN m.roles mr 
            WHERE(:keyword IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.email.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:email IS NULL OR LOWER(m.email) LIKE LOWER(CONCAT('%', :email, '%')))
            AND (:status IS NULL OR m.status = :status)
            AND (:role IS NULL OR EXISTS (
                SELECT 1 FROM MemberRole subMr 
                WHERE subMr.member = m AND subMr.roleType = :role
            ))
            ORDER BY m.createAt DESC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT m) FROM Member m
                    LEFT JOIN  m.roles mr
                    WHERE (:keyword IS NULL OR
                        LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(m.email.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
                    AND (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')))
                    AND (:email IS NULL OR LOWER(m.email.email) LIKE LOWER(CONCAT('%', :email, '%')))
                    AND (:status IS NULL OR m.status = :status)
                    AND (:role IS NULL OR EXISTS (
                          SELECT 1 FROM MemberRole subMr
                          WHERE subMr.member = m AND subMr.roleType = :role
                    ))
                    """)
    @QueryHints({
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
            @QueryHint(name = "org.hibernate.cacheRegion", value = "member.search"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "50")  // 배치 크기 최적화
    })
    Page<Member> searchMembers(
            @Param("keyword") String keyword,
            @Param("name") String name,
            @Param("email") String email,
            @Param("status") MemberStatus status,
            @Param("role") RoleType role,
            Pageable pageable
    );


    @Query(value = """
                    SELECT m FROM Member m 
                    JOIN FETCH m.roles mr 
                    WHERE m.id IN :memberIds
                    ORDER BY m.createAt DESC
            """)
    @QueryHints({
            @QueryHint(name = "org.hibernate.fetchSize", value = "100"),
            @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<Member> findMembersWithRoles(@Param("memberIds") List<Long> memberIds);
}
