package innercircle.member.infrastructure.adapter.in;

import innercircle.member.application.MemberCreateRequest;
import innercircle.member.application.MemberResponse;
import innercircle.member.application.port.in.MemberUseCase;
import innercircle.member.domain.member.MemberStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId,
                                                    @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
                                                    @RequestHeader(value = "X-EMAIL", required = false) String emailHeader,
                                                    @RequestHeader(value = "X-ROLES", required = false) String rolesHeader,
                                                    @RequestHeader(value = "X-AUTH-METHOD", required = false) String authMethodHeader) {

        // 🔍 헤더 정보 로깅
        log.info("=== Gateway 헤더 정보 ===");
        log.info("X-User-ID: {}", userIdHeader);
        log.info("X-EMAIL: {}", emailHeader);
        log.info("X-ROLES: {}", rolesHeader);
        log.info("X-AUTH-METHOD: {}", authMethodHeader);
        log.info("PathVariable memberId: {}", memberId);

        return ResponseEntity.ok(new MemberResponse(
                1L,
                "sw.noh@gmail.com",
                "노성웅",
                LocalDate.now(),
                "MAIL",
                MemberStatus.ACTIVE,
                LocalDateTime.now(),
                List.of("BUYER"))
        );
    }

}
