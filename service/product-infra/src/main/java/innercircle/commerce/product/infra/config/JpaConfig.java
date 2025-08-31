package innercircle.commerce.product.infra.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "innercircle.commerce.product.infra.repository")
@EntityScan(basePackages = "innercircle.commerce.product.infra.entity")
public class JpaConfig {
}