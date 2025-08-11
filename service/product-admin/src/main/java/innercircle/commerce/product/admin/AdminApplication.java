package innercircle.commerce.product.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"innercircle.commerce.product.core",
		"innercircle.commerce.product.admin",
		"innercircle.commerce.product.infra"
})
public class AdminApplication {
	public static void main (String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}
}
