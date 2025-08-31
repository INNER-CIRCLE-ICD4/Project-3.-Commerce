package innercircle.member.infrastructure.adapter.in.web.member.dto;

public record MemberCreateRequest(
        String email,
        String name,
        String password,
        String birthDate,
        String gender) {
}
