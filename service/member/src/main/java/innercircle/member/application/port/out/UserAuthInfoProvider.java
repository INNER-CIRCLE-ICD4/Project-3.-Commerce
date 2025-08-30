package innercircle.member.application.port.out;

import innercircle.member.domain.auth.UserAuthInfo;

public interface UserAuthInfoProvider {

    UserAuthInfo findByEmail(String email);

}
