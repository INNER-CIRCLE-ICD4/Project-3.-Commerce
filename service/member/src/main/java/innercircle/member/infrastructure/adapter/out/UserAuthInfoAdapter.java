package innercircle.member.infrastructure.adapter.out;

import innercircle.global.auth.AuthErrorCode;
import innercircle.member.application.port.out.MemberQueryPort;
import innercircle.member.application.port.out.UserAuthInfoProvider;
import innercircle.member.domain.auth.UserAuthInfo;
import innercircle.member.domain.auth.UserNotExistsException;
import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true) //todo 필요한가?
public class UserAuthInfoAdapter implements UserAuthInfoProvider {

    private final MemberQueryPort memberQueryPort;

    @Override
    public UserAuthInfo findByEmail(String email) {

        Member member = memberQueryPort.findByEmail(new Email(email))
                .orElseThrow(() -> new UserNotExistsException(AuthErrorCode.LOGIN_FAILED, "user not found. email=" + email));

        if (!member.getStatus().isActive()) {
            throw new IllegalArgumentException("사용자가 비활성화 상태입니다. user_status=" + member.getStatus());
        }

        return UserAuthInfo.create(
                member.getId(),
                member.getEmail().email(),
                member.getPassword(),
                member.getRoleNames(),
                member.isActive()
        );
    }
}

