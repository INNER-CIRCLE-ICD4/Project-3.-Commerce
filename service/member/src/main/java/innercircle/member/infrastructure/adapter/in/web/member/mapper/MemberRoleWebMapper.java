package innercircle.member.infrastructure.adapter.in.web.member.mapper;

import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberRole;
import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberRoleGrantResponse;
import org.springframework.stereotype.Component;

@Component
public class MemberRoleWebMapper {

    /**
     * Entity -> 권한 부여 응답 객체
     *
     * @return
     */
    public MemberRoleGrantResponse toMemberRoleGrantResponse(MemberRole memberRole) {

        Member member = memberRole.getMember();

        return new MemberRoleGrantResponse(
                member.getId(),
                member.getName(),
                member.getEmail().email(),
                memberRole.getRoleType().name(),
                memberRole.getAssignedAt()
        );
    }


}
