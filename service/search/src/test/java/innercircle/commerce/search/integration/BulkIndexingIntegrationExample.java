package innercircle.commerce.search.integration;

import innercircle.commerce.search.dto.ProductBulkIndexRequest;
import innercircle.commerce.search.dto.ProductRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 벌크 인덱싱 API 사용 예제
 * 
 * 이 클래스는 벌크 인덱싱 API를 어떻게 사용하는지 보여주는 예제입니다.
 * 실제 통합 테스트나 클라이언트 코드에서 참고할 수 있습니다.
 */
public class BulkIndexingIntegrationExample {

    /**
     * 벌크 인덱싱 요청 예제
     * 
     * POST /api/search/products/bulk-index
     * Content-Type: application/json
     */
    public static ProductBulkIndexRequest createBulkIndexRequest() {
        List<ProductRequest> products = new ArrayList<>();
        
        // 예제 1: 전자제품
        products.add(new ProductRequest(
                "PROD-001",
                "삼성 갤럭시 S24 Ultra",
                "GALAXY-S24U-256GB",
                "최신 플래그십 스마트폰. 256GB 저장공간, 12GB RAM, 200MP 카메라",
                1590000,
                List.of("전자제품", "스마트폰", "삼성"),
                "ACTIVE",
                "ONLINE"
        ));
        
        // 예제 2: 의류
        products.add(new ProductRequest(
                "PROD-002",
                "나이키 에어맥스 2024",
                "NIKE-AIRMAX-2024-280",
                "편안한 쿠셔닝과 스타일리시한 디자인의 러닝화",
                189000,
                List.of("의류", "신발", "스포츠"),
                "ACTIVE",
                "ONLINE"
        ));
        
        // 예제 3: 식품
        products.add(new ProductRequest(
                "PROD-003",
                "스타벅스 하우스 블렌드 원두",
                "STARBUCKS-HOUSE-1KG",
                "미디엄 로스트 원두. 부드럽고 균형잡힌 맛",
                25000,
                List.of("식품", "커피", "원두"),
                "ACTIVE",
                "ONLINE"
        ));
        
        // 예제 4: 가전제품
        products.add(new ProductRequest(
                "PROD-004",
                "LG 디오스 냉장고",
                "LG-DIOS-870L",
                "870L 대용량 4도어 냉장고. 에너지 효율 1등급",
                3200000,
                List.of("가전제품", "냉장고", "LG"),
                "ACTIVE",
                "OFFLINE"
        ));
        
        // 예제 5: 도서
        products.add(new ProductRequest(
                "PROD-005",
                "클린 코드 - 로버트 마틴",
                "BOOK-CLEAN-CODE",
                "소프트웨어 장인 정신과 클린 코드 작성법",
                35000,
                List.of("도서", "IT", "프로그래밍"),
                "ACTIVE",
                "ONLINE"
        ));
        
        return ProductBulkIndexRequest.builder()
                .products(products)
                .failOnError(false)  // 부분 실패 허용
                .build();
    }
    
    /**
     * 대량 데이터 벌크 인덱싱 요청 예제
     * 
     * 배치 처리나 초기 데이터 마이그레이션 시 사용
     */
    public static List<ProductBulkIndexRequest> createBatchRequests(int totalProducts) {
        List<ProductBulkIndexRequest> batches = new ArrayList<>();
        List<ProductRequest> currentBatch = new ArrayList<>();
        
        for (int i = 1; i <= totalProducts; i++) {
            currentBatch.add(new ProductRequest(
                    "BATCH-PROD-" + String.format("%06d", i),
                    "배치 상품 " + i,
                    "BATCH-CODE-" + i,
                    "배치 처리용 상품 설명 " + i,
                    10000 + (i * 100),
                    List.of("배치카테고리", "테스트"),
                    "ACTIVE",
                    "ONLINE"
            ));
            
            // 1000개씩 배치 생성 (최대 크기)
            if (currentBatch.size() == 1000 || i == totalProducts) {
                batches.add(ProductBulkIndexRequest.builder()
                        .products(new ArrayList<>(currentBatch))
                        .failOnError(false)
                        .build());
                currentBatch.clear();
            }
        }
        
        return batches;
    }
    
    /**
     * 응답 처리 예제
     */
    public static void handleBulkIndexResponse(String jsonResponse) {
        /*
        성공 응답 예시:
        {
            "totalRequested": 5,
            "successCount": 5,
            "failureCount": 0,
            "tookMillis": 250,
            "results": [
                {
                    "productId": "PROD-001",
                    "productName": "삼성 갤럭시 S24 Ultra",
                    "success": true,
                    "result": "CREATED"
                },
                ...
            ],
            "errors": []
        }
        
        부분 실패 응답 예시 (HTTP 207 Multi-Status):
        {
            "totalRequested": 5,
            "successCount": 3,
            "failureCount": 2,
            "tookMillis": 300,
            "results": [
                {
                    "productId": "PROD-001",
                    "productName": "삼성 갤럭시 S24 Ultra",
                    "success": true,
                    "result": "CREATED"
                },
                {
                    "productId": "PROD-002",
                    "productName": "나이키 에어맥스 2024",
                    "success": false,
                    "result": "FAILED",
                    "error": "Document parsing error"
                },
                ...
            ],
            "errors": ["부분 실패가 발생했습니다"]
        }
        */
    }
    
    /**
     * curl 명령어 예제
     */
    public static String getCurlExample() {
        return """
            curl -X POST http://localhost:8080/api/search/products/bulk-index \\
              -H "Content-Type: application/json" \\
              -d '{
                "products": [
                  {
                    "id": "PROD-001",
                    "name": "테스트 상품",
                    "code": "TEST-001",
                    "detailContent": "상품 설명",
                    "price": 10000,
                    "categories": ["전자제품"],
                    "productStatus": "ACTIVE",
                    "saleType": "ONLINE"
                  }
                ],
                "failOnError": false
              }'
            """;
    }
}