package innercircle.member.infrastructure.adapter.out;

import innercircle.global.ErrorCode;
import innercircle.member.application.port.out.MemberRepository;
import innercircle.member.application.port.out.UserAuthInfoProvider;
import innercircle.member.domain.auth.UserAuthInfo;
import innercircle.member.domain.auth.UserNotExistsException;
import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthInfoAdapter implements UserAuthInfoProvider {

    private final MemberRepository memberRepository;

    @Override
    public UserAuthInfo findByEmail(String email) {

        Member member = memberRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UserNotExistsException(ErrorCode.LOGIN_FAILED, "user not found. email=" + email));


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

