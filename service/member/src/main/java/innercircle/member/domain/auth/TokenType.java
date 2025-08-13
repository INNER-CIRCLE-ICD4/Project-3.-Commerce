package innercircle.member.domain.auth;

import lombok.Getter;

@Getter
public enum TokenType {
    ACCESS("Access Token", 3600),
    REFRESH("Refresh Token", 604800),
    ;

    private final String description;
    private final int defaultExpirySeconds;

    TokenType(String description, int defaultExpirySeconds) {
        this.description = description;
        this.defaultExpirySeconds = defaultExpirySeconds;
    }
}
