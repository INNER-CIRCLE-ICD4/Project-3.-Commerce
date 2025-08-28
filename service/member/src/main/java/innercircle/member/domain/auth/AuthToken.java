package innercircle.member.domain.auth;

public record AuthToken(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
    public AuthToken {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be null or blank");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be null or blank");
        }
    }
}
