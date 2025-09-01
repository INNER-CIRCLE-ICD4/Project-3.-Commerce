package innercircle.commerce.order.infra.adapter.external;

import innercircle.commerce.order.application.port.out.ProductService;
import innercircle.commerce.order.domain.model.vo.ProductId;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * ProductServiceAdapter
 * 상품 서비스와 통신하는 어댑터
 */
@Component
public class ProductServiceAdapter implements ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceAdapter.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${external.service.product.url:http://localhost:8081}")
    private String productServiceUrl;
    
    public ProductServiceAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public boolean checkAvailability(ProductId productId, int quantity) {
        try {
//            String url = productServiceUrl + "/api/v1/products/{productId}/availability";
//            Map<String, Object> request = new HashMap<>();
//            request.put("quantity", quantity);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
//
//            ResponseEntity<AvailabilityResponse> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.POST,
//                    entity,
//                    AvailabilityResponse.class,
//                    productId.getValue()
//            );
//
//            return response.getBody() != null && response.getBody().isAvailable();
            
            // 테스트용 코드
            return true;
        } catch (Exception e) {
            logger.error("Failed to check product availability for product: {}", productId, e);
            return false;
        }
    }
    
    @Override
    public void reserveStock(ProductId productId, int quantity) {
        try {
//            String url = productServiceUrl + "/api/v1/products/{productId}/reserve";
//            Map<String, Object> request = new HashMap<>();
//            request.put("quantity", quantity);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
//
//            restTemplate.exchange(
//                    url,
//                    HttpMethod.POST,
//                    entity,
//                    Void.class,
//                    productId.getValue()
//            );
//
//            logger.info("Successfully reserved {} units of product: {}", quantity, productId);
        } catch (Exception e) {
            logger.error("Failed to reserve stock for product: {}", productId, e);
            throw new RuntimeException("Stock reservation failed", e);
        }
    }
    
    @Override
    public void releaseStock(ProductId productId, int quantity) {
        try {
//            String url = productServiceUrl + "/api/v1/products/{productId}/release";
//            Map<String, Object> request = new HashMap<>();
//            request.put("quantity", quantity);
//            
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
//            
//            restTemplate.exchange(
//                    url,
//                    HttpMethod.POST,
//                    entity,
//                    Void.class,
//                    productId.getValue()
//            );
            
            logger.info("Successfully released {} units of product: {}", quantity, productId);
        } catch (Exception e) {
            logger.error("Failed to release stock for product: {}", productId, e);
            throw new RuntimeException("Stock release failed", e);
        }
    }
    
    @Override
    public ProductInfo getProductInfo(ProductId productId) {
//        String url = productServiceUrl + "/api/v1/products/{productId}";
//
//        try {
//            ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
//                    url,
//                    ProductResponse.class,
//                    productId.getValue()
//            );
//
//            if (response.getBody() != null) {
//                ProductResponse product = response.getBody();
//                return new ProductInfo(
//                        product.getId(),
//                        product.getName(),
//                        product.getDescription(),
//                        new Money(product.getPrice()),
//                        product.getStockQuantity(),
//                        product.isAvailable()
//                );
//            }
//
//            throw new RuntimeException("Product not found: " + productId);
//        } catch (Exception e) {
//            logger.error("Failed to get product info for product: {}", productId, e);
//            throw new RuntimeException("Failed to get product info", e);
//        }
        
        // 테스트를 위한 임의 값 반환
        return new ProductInfo(
            productId.getValue(),
            "테스트 상품 - " + productId.getValue(),
            "테스트 상품 설명",
            new Money(new BigDecimal("15000")),
            100,
            true
        );
    }
    
    @Override
    public ProductOptionInfo getProductOptionInfo(ProductId productId, Long productOptionId) {
//        String url = productServiceUrl + "/api/v1/products/{productId}/options/{optionId}";
//
//        try {
//            ResponseEntity<ProductOptionResponse> response = restTemplate.getForEntity(
//                    url,
//                    ProductOptionResponse.class,
//                    productId.getValue(),
//                    productOptionId
//            );
//
//            if (response.getBody() != null) {
//                ProductOptionResponse option = response.getBody();
//                return new ProductOptionInfo(
//                        option.getId(),
//                        option.getName(),
//                        new Money(option.getPrice()),
//                        new Money(option.getDiscountPrice()),
//                        option.getStockQuantity(),
//                        option.isAvailable()
//                );
//            }
//
//            throw new RuntimeException("Product option not found: " + productId + ", " + productOptionId);
//        } catch (Exception e) {
//            logger.error("Failed to get product option info for product: {}, option: {}", productId, productOptionId, e);
//            throw new RuntimeException("Failed to get product option info", e);
//        }
        
        // 테스트를 위한 임의 값 반환
        return new ProductOptionInfo(
            productOptionId,
            "테스트 옵션 - " + productOptionId,
            new Money(new BigDecimal("10000")),
            new Money(new BigDecimal("1000")),
            100,
            true
        );
    }
    
    @Getter
    @Setter
    private static class AvailabilityResponse {
        private boolean available;
        private int availableQuantity;
    }

    @Getter
    @Setter
    private static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private int stockQuantity;
        private boolean available;
    }
    
    @Getter
    @Setter
    private static class ProductOptionResponse {
        private Long id;
        private String name;
        private BigDecimal price;
        private BigDecimal discountPrice;
        private int stockQuantity;
        private boolean available;
    }
}
