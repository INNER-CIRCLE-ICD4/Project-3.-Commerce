package innercircle.commerce.context.support;

import innercircle.common.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) { // 2. 어노테이션의 파라미터 값을 customUser 객체로 받음

        // 3. 빈 SecurityContext를 생성
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // 4. 어노테이션의 값(customUser.memberId())을 사용해 CustomUserDetails 객체를 생성
        AuthenticatedUser userDetails = new AuthenticatedUser(
                customUser.memberId(),
                customUser.username(),
                List.of(customUser.roles()),
                customUser.authMethod()
        );

        // 5. Spring Security가 이해할 수 있는 Authentication 객체를 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                ""
        );

        // 6. SecurityContext에 만들어진 Authentication 객체를 넣어줌
        context.setAuthentication(authentication);

        // 7. 완성된 SecurityContext를 반환
        return context;
    }
}
