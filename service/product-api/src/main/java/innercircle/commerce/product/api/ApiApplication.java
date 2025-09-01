package innercircle.commerce.product.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"innercircle.commerce.product.infra.entity"})
public class ApiApplication {
	public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
	}
}
