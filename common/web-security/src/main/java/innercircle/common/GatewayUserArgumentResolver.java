package innercircle.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class GatewayUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String EMAIL_HEADER = "X-EMAIL";
    private static final String ROLES_HEADER = "X-ROLES";
    private static final String AUTH_METHOD_HEADER = "X-AUTH-METHOD";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
                parameter.getParameterType().equals(AuthenticatedUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        CurrentUser annotation = parameter.getParameterType().getAnnotation(CurrentUser.class);
        boolean required = annotation != null && annotation.required();

        String userIdHeader = webRequest.getHeader(USER_ID_HEADER);
        String emailHeader = webRequest.getHeader(EMAIL_HEADER);
        String rolesHeader = webRequest.getHeader(ROLES_HEADER);
        String authMethodHeader = webRequest.getHeader(AUTH_METHOD_HEADER);

        log.debug("인증 헤더 정보 - user_id: {}, email: {}, roles: {}, auth_method: {}",userIdHeader, emailHeader, rolesHeader, authMethodHeader);

        if (!StringUtils.hasText(userIdHeader) || !StringUtils.hasText(emailHeader)) {
            if (required) {
                log.warn("필수 인증 헤더 누락 - user_id: {}, email: {}", userIdHeader, emailHeader);
                throw new UnauthorizedException("필수 인증 헤더가 누락되었습니다. " + "X-User-ID: " + userIdHeader + ", X-EMAIL: " + emailHeader);
            }
            log.debug("선택적 인증 헤더가 누락됨");
            return null;
        }

        try {
            Long userId = Long.parseLong(userIdHeader);
            List<String> roles = StringUtils.hasText(rolesHeader) ?
                    Arrays.asList(rolesHeader.split(",")) : List.of();

            AuthenticatedUser user = new AuthenticatedUser(userId, emailHeader, roles, authMethodHeader);
            log.debug("인증된 사용자: {}", user);

            return user;

        } catch (NumberFormatException e) {
            log.error("잘못된 사용자 ID 형식: {}", userIdHeader, e);
            if (required) {
                throw new ForbiddenException("유효하지 않은 사용자 정보입니다");
            }
            return null;
        }
    }
}
