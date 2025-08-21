package innercircle.member.domain.member;

import innercircle.global.member.MemberErrorCode;
import innercircle.member.application.port.out.MemberQueryRepository;
import innercircle.member.application.port.out.MemberRepository;
import innercircle.member.application.port.out.PasswordEncoderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberDomainService {

    private final MemberQueryRepository memberQueryRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public boolean existsByEmail(String email) {

        Email requestEmail = new Email(email);
        Optional<Member> byEmail = memberQueryRepository.findByEmailUsingNaturalId(requestEmail);

        if (byEmail.isPresent()) throw new DuplicateRequestException(MemberErrorCode.DUPLICATE_EMAIL, "이미 존재하는 이메일입니다. email=" + requestEmail.email());

        return true;
    }

    public String encodePassword(String password) {
        return passwordEncoderPort.encode(password);
    }
}
