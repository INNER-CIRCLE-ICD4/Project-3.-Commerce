package innercircle.member.infrastructure.adapter.in.web.auth.dto;



public record LoginResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
    public LoginResponse {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be null or blank");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be null or blank");
        }
    }
}
