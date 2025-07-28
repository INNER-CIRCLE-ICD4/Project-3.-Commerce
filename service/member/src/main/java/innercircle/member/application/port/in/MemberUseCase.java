package innercircle.member.application.port.in;

import innercircle.member.application.MemberCreateRequest;
import innercircle.member.application.MemberResponse;

public interface MemberUseCase {

    MemberResponse createMember(MemberCreateRequest request);

}
