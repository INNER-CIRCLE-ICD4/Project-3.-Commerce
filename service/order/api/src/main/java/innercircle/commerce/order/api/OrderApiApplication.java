package innercircle.commerce.order.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Order API Application
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "innercircle.commerce.order.api",
    "innercircle.commerce.order.application",
    "innercircle.commerce.order.infra"
})
@EntityScan(basePackages = "innercircle.commerce.order.infra.adapter.persistence.entity")
@EnableJpaRepositories(basePackages = "innercircle.commerce.order.infra.adapter.persistence.repository")
public class OrderApiApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OrderApiApplication.class, args);
    }
}
