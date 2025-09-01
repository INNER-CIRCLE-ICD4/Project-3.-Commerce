package innercircle.commerce.global.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${feign.client.url.product-service}")
public interface ProductServiceClient {
    @GetMapping("/api/products/{productId}/exists")
    boolean checkProductExists(@PathVariable("productId") Long productId);
}
