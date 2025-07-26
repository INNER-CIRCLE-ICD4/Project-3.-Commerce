package innercircle.member.domain;

import com.sun.jdi.request.DuplicateRequestException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MemberDomainService {


    public boolean existsByEmail(String email, MemberRepository memberRepository) {

        Email requestEmail = new Email(email);

        Optional<Member> byEmail = memberRepository.findByEmail(requestEmail);

        if(byEmail.isPresent()) {
            throw new DuplicateRequestException("이미 가입된 이메일입니다. email: " + email);
        }

        return true;
    }
}
