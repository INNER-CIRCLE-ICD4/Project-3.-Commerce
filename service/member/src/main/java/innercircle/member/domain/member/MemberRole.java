package innercircle.member.domain.member;

import innercircle.global.member.MemberErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_role",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_MEMBER_ROLE_TYPE",
                        columnNames = {"member_id", "role_type"}
                )
        }
)
public class MemberRole extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    private LocalDateTime assignedAt;

    public static MemberRole signUp(Member member) {
        MemberRole memberRole = createMemberRole(RoleType.BUYER);
        memberRole.member = member;
        member.getRoles().add(memberRole);

        return memberRole;
    }

    public static MemberRole grantAdminRole(Member member) {
        MemberRole memberAdminRole = createMemberRole(RoleType.ADMIN);
        if (member.hasRole(RoleType.ADMIN)) {
            throw new DuplicateRoleException(MemberErrorCode.DUPLICATE_ROLE, "Duplicate Admin Role, MemberId: {}" + member.getId());
        }
        memberAdminRole.member = member;
        member.getRoles().add(memberAdminRole);
        return memberAdminRole;
    }

    public static MemberRole grantSellerRole(Member member) {
        MemberRole memberAdminRole = createMemberRole(RoleType.SELLER);
        if (member.hasRole(RoleType.SELLER)) {
            throw new DuplicateRoleException(MemberErrorCode.DUPLICATE_ROLE, "Duplicate Seller Role, MemberId: {}" + member.getId());
        }
        memberAdminRole.member = member;
        member.getRoles().add(memberAdminRole);
        return memberAdminRole;
    }


    private static MemberRole createMemberRole(RoleType roleType) {
        MemberRole memberRole = new MemberRole();
        memberRole.roleType = roleType;
        memberRole.assignedAt = LocalDateTime.now();
        return memberRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberRole)) return false;
        return Objects.equals(getId(), ((MemberRole) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
