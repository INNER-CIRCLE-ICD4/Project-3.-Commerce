package innercircle.commerce.product.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EntityScan(basePackages = {"innercircle.commerce.product.infra.entity"})
@EnableAsync
public class AdminApplication {
	public static void main (String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}
}
