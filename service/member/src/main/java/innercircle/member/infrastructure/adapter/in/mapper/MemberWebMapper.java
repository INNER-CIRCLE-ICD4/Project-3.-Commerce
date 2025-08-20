package innercircle.member.infrastructure.adapter.in.mapper;

import innercircle.member.application.MemberCreateRequest;
import innercircle.member.application.MemberCreateResponse;
import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberRole;
import org.springframework.stereotype.Component;

@Component
public class MemberWebMapper {

    /**
     * request -> entity
     */
    public Member createRequestToEntity(MemberCreateRequest request) {
        return Member.create(
                request.email(),
                request.name(),
                request.password(),
                request.birthDate(),
                request.gender()
        );
    }

    /**
     * entity -> response
     */
    public MemberCreateResponse entityToCreateResponse(Member member) {
        return new MemberCreateResponse(
                member.getId(),
                member.getEmail().email(),
                member.getName(),
                member.getBirthDate(),
                member.getGender().name(),
                member.getStatus(),
                member.getCreateAt(),
                member.getRoles().stream()
                        .map(MemberRole::getRoleType)
                        .map(Enum::name)
                        .toList()
        );
    }
}

