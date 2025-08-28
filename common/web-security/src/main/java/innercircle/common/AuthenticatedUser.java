package innercircle.common;

import java.util.Arrays;
import java.util.List;

public record AuthenticatedUser(
        Long userId,
        String email,
        List<String> roles,
        String authMethod
) {
    public boolean heaRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean hasAnyRole(String... roles) {
        if (this.roles == null || this.roles.isEmpty()) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(this::heaRole);
    }

    public boolean isAdmin() {
        return hasAnyRole("ROLE_ADMIN");
    }

    public boolean isUser() {
        return hasAnyRole("ROLE_USER");
    }

    public boolean isSeller() {
        return hasAnyRole("ROLE_SELLER");
    }
}
