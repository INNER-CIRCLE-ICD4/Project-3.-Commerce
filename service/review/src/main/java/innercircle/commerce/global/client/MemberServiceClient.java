package innercircle.commerce.global.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", url = "${feign.client.url.member-service}")
public interface MemberServiceClient {
    @GetMapping("/api/members/{memberId}/exists")
    boolean checkMemberExists(@PathVariable("memberId") Long memberId);
}
