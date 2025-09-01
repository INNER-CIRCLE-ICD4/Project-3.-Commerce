package innercircle.commerce.product.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
		"innercircle.commerce.product.core",
		"innercircle.commerce.product.api",
		"innercircle.commerce.product.infra"
})
public class ProductApiConfig {
}
