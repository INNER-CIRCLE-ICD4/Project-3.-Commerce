package innercircle.member.infrastructure.adapter.in;

import innercircle.common.AuthenticatedUser;
import innercircle.common.CurrentUser;
import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberCreateRequest;
import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberCreateResponse;
import innercircle.member.application.port.in.MemberUseCase;
import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberStatus;
import innercircle.member.infrastructure.adapter.in.web.member.mapper.MemberWebMapper;
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
    private final MemberWebMapper memberWebMapper;

    @PostMapping
    public ResponseEntity<MemberCreateResponse> createMember(@RequestBody MemberCreateRequest request) {

        Member member = memberUseCase.createMember(
                memberWebMapper.createRequestToEntity(request)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(memberWebMapper.entityToCreateResponse(member));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberCreateResponse> getMember(@PathVariable Long memberId,
                                                          @CurrentUser AuthenticatedUser authenticatedUser) {
        // 🔍 헤더 정보 로깅
        log.info("=== Gateway 헤더 정보 ===");
        log.info("X-User-ID: {}", authenticatedUser.userId());
        log.info("X-EMAIL: {}", authenticatedUser.email());
        log.info("X-AUTH-METHOD: {}", authenticatedUser.authMethod());
        log.info("PathVariable memberId: {}", memberId);

        return ResponseEntity.ok(new MemberCreateResponse(
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
