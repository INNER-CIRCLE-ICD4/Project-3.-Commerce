package innercircle.commerce.product.infra.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정
 */
@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = "innercircle.commerce.product.infra.entity")
public class JpaConfig {
}