package innercircle.member.application;

import innercircle.member.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MemberResponse (
        Long memberId,
        String email,
        String name,
        LocalDate birthDate,
        String gender,
        MemberStatus status,
        LocalDateTime createAt,
        List<String> memberRoles) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail().email(),
                member.getName(),
                member.getBirthDate(),
                member.getGender().name(),
                member.getStatus(),
                member.getCreateAt(),
                member.getRoles().stream()
                        .map(MemberRole::getRoleType)
                        .map(Enum::name)
                        .toList()
        );
    }
}
