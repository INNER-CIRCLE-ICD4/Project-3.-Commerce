package innercircle.member.infrastructure.adapter.in.web.member.dto;


import innercircle.member.domain.member.MemberStatus;

import java.time.LocalDateTime;
import java.util.List;

public record MemberSearchResponse(
        Long memberId,
        String email,
        String name,
        String gender,
        MemberStatus status,
        LocalDateTime createAt,
        List<String> memberRoles
) {


    public static MemberSearchResponse of(
            Long memberId,
            String email,
            String name,
            String gender,
            MemberStatus status,
            LocalDateTime createAt,
            List<String> roles) {
        return new MemberSearchResponse(memberId, email, name, gender, status, createAt, roles);
    }
}
