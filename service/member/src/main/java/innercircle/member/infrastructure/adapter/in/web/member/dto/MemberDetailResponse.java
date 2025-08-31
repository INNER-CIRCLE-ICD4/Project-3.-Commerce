package innercircle.member.infrastructure.adapter.in.web.member.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MemberDetailResponse(
        Long memberId,
        String email,
        String name,
        String gender,
        LocalDate birth,
        LocalDateTime createAt,
        List<String> roles
) {
    public static MemberDetailResponse of(Long memberId, String email, String name, String gender, LocalDate birth, LocalDateTime createAt, List<String> roles) {
        return new MemberDetailResponse(memberId, email, name, gender, birth, createAt, roles);
    }
}
