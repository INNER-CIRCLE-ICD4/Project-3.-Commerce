package innercircle.commerce.product.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "innercircle.commerce.product.core.domain")
@EnableJpaRepositories(basePackages = "innercircle.commerce.product.core.repository")
public class ProductCoreConfig {
}