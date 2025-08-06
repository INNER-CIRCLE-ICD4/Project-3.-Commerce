package innercircle.member.application;

public record MemberCreateRequest(
        String email,
        String name,
        String password,
        String birthDate,
        String gender) {
}
