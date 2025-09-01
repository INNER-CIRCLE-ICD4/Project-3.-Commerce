package innercircle.member.infrastructure.adapter.in.web.member.mapper;

import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberCreateRequest;
import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberCreateResponse;
import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberRole;
import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberDetailResponse;
import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
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

    /**
     * Page[entity] -> Page[SearchResponse]
     */
    public Page<MemberSearchResponse> entityToSearchResponse(Page<Member> members) {

        List<MemberSearchResponse> memberSearchResponses = members.stream()
                .parallel()
                .map(member -> MemberSearchResponse.of(member.getId(), member.getEmail().email(), member.getName(), member.getGender().name(), member.getStatus(), member.getCreateAt(), member.getRoleNames()))
                .toList();

        return new PageImpl<>(memberSearchResponses, members.getPageable(), members.getTotalElements());
    }

    /**
     * Entity -> DetailResponse
     */
    public MemberDetailResponse entityToDetailResponse(Member member) {

        return MemberDetailResponse.of(
                member.getId(),
                member.getEmail().email(),
                member.getName(),
                member.getGender().name(),
                member.getBirthDate(),
                member.getCreateAt(),
                member.getRoleNames()
        );
    }
}

