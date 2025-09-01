package innercircle.commerce.order.api.config;

import innercircle.commerce.common.snowflake.Snowflake;
import innercircle.commerce.order.application.port.out.ProductService;
import innercircle.commerce.order.infra.adapter.external.ProductServiceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * BeanConfig
 * 애플리케이션 빈 설정
 */
@Configuration
public class BeanConfig {
    
    @Bean
    public Snowflake snowflake() {
        return new Snowflake();
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public ProductService productService(RestTemplate restTemplate) {
        return new ProductServiceAdapter(restTemplate);
    }
}
