package innercircle.member.infrastructure.adapter.in.web.auth.dto;

public record RefreshRequest(String refreshToken) {

    public RefreshRequest {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be null or blank");
        }
    }
}
