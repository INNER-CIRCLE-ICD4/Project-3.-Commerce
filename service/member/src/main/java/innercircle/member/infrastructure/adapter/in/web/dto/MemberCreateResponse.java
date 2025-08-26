package innercircle.member.infrastructure.adapter.in.web.dto;

import innercircle.member.domain.member.MemberStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MemberCreateResponse(
        Long memberId,
        String email,
        String name,
        LocalDate birthDate,
        String gender,
        MemberStatus status,
        LocalDateTime createAt,
        List<String> memberRoles) {

}
