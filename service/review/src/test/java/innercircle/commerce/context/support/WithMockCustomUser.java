package innercircle.commerce.context.support;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    long memberId() default 1L;

    String username() default "testuser";

    String email() default "testuser@test.com";

    String authMethod() default "JWT";

    String[] roles() default {"ROLE_USER"};
}
