package innercircle.member.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRole extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    private LocalDateTime assignedAt;

    public static MemberRole signUp(Member member) {
        MemberRole memberRole = new MemberRole();
        memberRole.roleType = RoleType.BUYER;
        memberRole.assignedAt = LocalDateTime.now();

        memberRole.member = member;
        member.getRoles().add(memberRole);

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
