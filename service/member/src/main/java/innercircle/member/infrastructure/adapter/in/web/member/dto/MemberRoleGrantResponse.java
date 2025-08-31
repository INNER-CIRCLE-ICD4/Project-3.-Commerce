package innercircle.member.infrastructure.adapter.in.web.member.dto;


import java.time.LocalDateTime;

public record MemberRoleGrantResponse(
        Long memberId,
        String name,
        String email,
        String roleType,
        LocalDateTime grantedAt
) {
}
