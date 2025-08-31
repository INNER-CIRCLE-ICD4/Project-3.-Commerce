package innercircle.member.domain.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAuthInfo {

    private Long userId;
    private String email;
    private String encodedPassword;
    private List<String> roles = new ArrayList<>();
    private boolean active;


    public static UserAuthInfo create(Long userId, String email, String encodedPassword, List<String> roles, boolean active) {
        //todo: 이메일 형식 검증 추가 필요
        UserAuthInfo userAuthInfo = new UserAuthInfo();
        userAuthInfo.userId = userId;
        userAuthInfo.email = email;
        userAuthInfo.encodedPassword = encodedPassword;
        userAuthInfo.roles = roles;
        userAuthInfo.active = active;

        return userAuthInfo;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

}
