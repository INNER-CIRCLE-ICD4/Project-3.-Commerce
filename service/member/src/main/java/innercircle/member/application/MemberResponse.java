package innercircle.member.application;

import innercircle.member.domain.Email;
import innercircle.member.domain.Gender;
import innercircle.member.domain.Member;
import innercircle.member.domain.MemberStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberResponse (
        Long memberId,
        Email email,
        String name,
        LocalDate birthDate,
        Gender gender,
        MemberStatus status,
        LocalDateTime createAt) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getBirthDate(),
                member.getGender(),
                member.getStatus(),
                member.getCreateAt()
        );
    }
}
