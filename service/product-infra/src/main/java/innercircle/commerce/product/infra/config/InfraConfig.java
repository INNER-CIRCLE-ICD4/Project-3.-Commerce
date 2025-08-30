package innercircle.commerce.product.infra.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
		"innercircle.commerce.product.core",
		"innercircle.commerce.product.infra"
})
public class InfraConfig {
}
