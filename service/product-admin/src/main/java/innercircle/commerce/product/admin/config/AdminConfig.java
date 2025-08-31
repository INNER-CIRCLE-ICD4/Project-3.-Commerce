package innercircle.commerce.product.admin.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
		"innercircle.commerce.product.core",
		"innercircle.commerce.product.admin",
		"innercircle.commerce.product.infra"
})
public class AdminConfig {
}
