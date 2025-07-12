# Snowflake ID Generator

## 📌 개요

Snowflake는 Twitter에서 개발한 분산 시스템용 고유 ID 생성 알고리즘입니다. 이 알고리즘은 분산 환경에서 중복 없이 시간순으로 정렬 가능한 64비트 정수 ID를 생성합니다.

## 🎯 왜 Snowflake를 사용하는가?

### 기존 방식의 문제점

1. **Auto Increment (자동 증가)**
   - 단일 데이터베이스에 의존적
   - 분산 환경에서 중복 발생 가능
   - 확장성 제한

2. **UUID (Universally Unique Identifier)**
   - 128비트로 크기가 큼
   - 시간순 정렬 불가능
   - 인덱싱 성능 저하

### Snowflake의 장점

- ✅ **분산 환경 지원**: 여러 서버에서 동시에 ID 생성 가능
- ✅ **시간순 정렬**: 생성 시간 기준으로 자연스럽게 정렬
- ✅ **고성능**: 초당 수백만 개의 ID 생성 가능
- ✅ **작은 크기**: 64비트 정수로 저장 공간 효율적

## 🔧 구조

Snowflake ID는 64비트 정수로 다음과 같이 구성됩니다:

```
┌─┬──────────────────────────────────────────────┬─────────────┬──────────────┐
│0│                  Timestamp (41 bits)          │ NodeID (10) │ Sequence(12) │
└─┴──────────────────────────────────────────────┴─────────────┴──────────────┘
```

- **Sign bit (1 bit)**: 항상 0 (양수 보장)
- **Timestamp (41 bits)**: Epoch 이후 경과한 밀리초
- **Node ID (10 bits)**: 서버/노드 식별자 (최대 1,024개)
- **Sequence (12 bits)**: 같은 밀리초 내 순서 (최대 4,096개)

## 💻 우리 프로젝트에서의 구현

```java
public class Snowflake {
    private static final int UNUSED_BITS = 1;
    private static final int EPOCH_BITS = 41;
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    
    // 2024-01-01 00:00:00 UTC 기준
    private final long startTimeMillis = 1704067200000L;
    
    public synchronized long nextId() {
        // ID 생성 로직
    }
}
```

## 🚀 사용 예시

### 1. 주문 서비스 (Order Service)
```java
@Service
public class OrderService {
    private final Snowflake snowflake;
    
    public Order createOrder(OrderRequest request) {
        Long orderId = snowflake.nextId();
        Order order = Order.builder()
            .id(orderId)
            .userId(request.getUserId())
            .createdAt(LocalDateTime.now())
            .build();
        
        return orderRepository.save(order);
    }
}
```

### 2. 상품 서비스 (Product Service)
```java
@Service
public class ProductService {
    private final Snowflake snowflake;
    
    public Product registerProduct(ProductRequest request) {
        Long productId = snowflake.nextId();
        Product product = Product.builder()
            .id(productId)
            .name(request.getName())
            .price(request.getPrice())
            .build();
        
        return productRepository.save(product);
    }
}
```

### 3. 리뷰 서비스 (Review Service)
```java
@Service
public class ReviewService {
    private final Snowflake snowflake;
    
    public Review writeReview(ReviewRequest request) {
        Long reviewId = snowflake.nextId();
        Review review = Review.builder()
            .id(reviewId)
            .productId(request.getProductId())
            .userId(request.getUserId())
            .content(request.getContent())
            .rating(request.getRating())
            .build();
        
        return reviewRepository.save(review);
    }
}
```

## 📊 성능 특성

- **생성 속도**: 단일 노드에서 초당 최대 409만 개 ID 생성 가능
- **유효 기간**: 2024년부터 약 69년간 사용 가능
- **노드 수**: 최대 1,024개의 독립적인 노드 지원

## 🔍 ID 분석 도구

생성된 ID에서 정보를 추출하는 유틸리티:

```java
public class SnowflakeAnalyzer {
    public static void analyze(long id) {
        long timestamp = (id >> 22) + START_EPOCH;
        long nodeId = (id >> 12) & 0x3FF;
        long sequence = id & 0xFFF;
        
        System.out.println("Timestamp: " + new Date(timestamp));
        System.out.println("Node ID: " + nodeId);
        System.out.println("Sequence: " + sequence);
    }
}
```

## ⚠️ 주의사항

1. **시간 동기화**: 모든 서버의 시간이 동기화되어 있어야 함
2. **노드 ID 관리**: 각 서비스/인스턴스별로 고유한 노드 ID 할당 필요
3. **Clock Backward**: 시스템 시간이 과거로 돌아가는 경우 처리 필요

## 🎯 결론

Snowflake ID Generator는 마이크로서비스 아키텍처에서 각 서비스가 독립적으로 고유 ID를 생성할 수 있게 해주는 핵심 컴포넌트입니다. 이를 통해:

- 데이터베이스 의존성 제거
- 서비스 간 결합도 감소
- 수평적 확장 용이
- 일관된 ID 체계 유지

우리 이커머스 플랫폼의 모든 엔티티(주문, 상품, 리뷰 등)는 이 Snowflake ID를 사용하여 분산 환경에서도 안정적으로 운영될 수 있습니다.