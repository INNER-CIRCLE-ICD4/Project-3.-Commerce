package innercircle.member.infrastructure.adapter.in;

import innercircle.member.application.port.in.MemberUseCase;
import innercircle.member.domain.member.MemberRole;
import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberRoleGrantResponse;
import innercircle.member.infrastructure.adapter.in.web.member.mapper.MemberRoleWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/grant")
@RestController
@RequiredArgsConstructor
public class RoleController {

    private final MemberRoleWebMapper memberRoleWebMapper;
    private final MemberUseCase memberUseCase;


    @PostMapping("/admin/{memberId}")
    public ResponseEntity<MemberRoleGrantResponse> grantAdminRole(@PathVariable Long memberId) {

        MemberRole memberRole = memberUseCase.grantAdminRole(memberId);

        return ResponseEntity.ok(memberRoleWebMapper.toMemberRoleGrantResponse(memberRole));
    }

    @PostMapping("/seller/{memberId}")
    public ResponseEntity<MemberRoleGrantResponse> grantSellerRole(@PathVariable Long memberId) {

        MemberRole memberRole = memberUseCase.grantSellerRole(memberId);

        return ResponseEntity.ok(memberRoleWebMapper.toMemberRoleGrantResponse(memberRole));
    }


}
