package innercircle.member.domain.member;

import innercircle.global.member.MemberErrorCode;
import innercircle.member.application.port.out.MemberQueryPort;
import innercircle.member.application.port.out.PasswordEncoderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberDomainService {

    private final MemberQueryPort memberQueryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public boolean existsByEmail(String email) {

        Email requestEmail = new Email(email);
        Optional<Member> byEmail = memberQueryPort.findByEmailUsingNaturalId(requestEmail);

        if (byEmail.isPresent()) throw new DuplicateRequestException(MemberErrorCode.DUPLICATE_EMAIL, "이미 존재하는 이메일입니다. email=" + requestEmail.email());

        return true;
    }

    public String encodePassword(String password) {
        return passwordEncoderPort.encode(password);
    }
}
