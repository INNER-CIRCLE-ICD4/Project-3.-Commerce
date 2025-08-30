package innercircle.member.infrastructure.adapter.in.web.member.dto;


import innercircle.member.domain.member.MemberStatus;
import innercircle.member.domain.member.RoleType;

public record MemberSearchRequest(
        String keyword,
        String email,
        String name,
        MemberStatus memberStatus,
        RoleType role,
        int page,
        int size) {

    public MemberSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
    }

    /**
     * 기본 검색 (키워드만)
     */
    public static MemberSearchRequest ofKeyword(String keyword) {
        return new MemberSearchRequest(keyword, null, null, null, null, 0, 20);
    }

    /**
     * 빈 조건 검색인지 확인
     */
    public boolean isEmpty() {
        return (keyword == null || keyword.trim().isEmpty()) &&
                (name == null || name.trim().isEmpty()) &&
                (email == null || email.trim().isEmpty()) &&
                memberStatus == null &&
                role == null;
    }

}
