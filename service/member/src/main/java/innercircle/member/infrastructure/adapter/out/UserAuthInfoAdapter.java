package innercircle.member.infrastructure.adapter.out;

import innercircle.member.application.port.out.MemberRepository;
import innercircle.member.application.port.out.UserAuthInfoProvider;
import innercircle.member.domain.auth.UserAuthInfo;
import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthInfoAdapter implements UserAuthInfoProvider {

    private final MemberRepository memberRepository;

    @Override
    public UserAuthInfo findByEmail(String email) {

        Member member = memberRepository.findByEmail(new Email(email)).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 계정입니다."));

        return UserAuthInfo.create(
                member.getId(),
                member.getEmail().email(),
                member.getPassword(),
                member.getRoleNames(),
                member.isActive()
        );
    }
}
