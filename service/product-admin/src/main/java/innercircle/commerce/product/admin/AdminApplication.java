package innercircle.commerce.product.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"innercircle.commerce.product.infra.entity"})
public class AdminApplication {
	public static void main (String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}
}
