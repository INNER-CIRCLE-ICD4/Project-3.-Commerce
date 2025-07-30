package innercircle.member.infrastructure.adapter.in;

import innercircle.member.application.MemberCreateRequest;
import innercircle.member.application.MemberResponse;
import innercircle.member.application.port.in.MemberUseCase;
import innercircle.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberUseCase memberUseCase;

    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@RequestBody MemberCreateRequest request) {
        log.info("회원가입 요청 : {}", request.email());

        MemberResponse member = memberUseCase.createMember(request);

        log.info("회원가입 완료, member_id: {}, email: {}", member.memberId(), member.email());

        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }
}
