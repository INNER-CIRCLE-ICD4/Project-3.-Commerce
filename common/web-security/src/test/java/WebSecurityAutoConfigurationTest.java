import innercircle.common.GatewayUserArgumentResolver;
import innercircle.common.WebSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class WebSecurityAutoConfigurationTest {
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebSecurityAutoConfiguration.class));

    @Test
    void contextLoads() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(WebSecurityAutoConfiguration.class);
            assertThat(context).hasSingleBean(GatewayUserArgumentResolver.class);
        });
    }
}
