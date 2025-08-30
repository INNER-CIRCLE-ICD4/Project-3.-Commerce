# 보상 트랜잭션 (Compensating Transaction) 적용 가이드

## 1. 보상 트랜잭션이란?

보상 트랜잭션은 분산 시스템 환경에서 데이터의 일관성을 유지하기 위한 패턴입니다. 특정 작업이 여러 단계에 걸쳐 여러 시스템(예: 데이터베이스, 외부 파일 스토리지, 메시지 큐)과 상호작용할 때, 중간 단계에서 실패가 발생하면 **이미 성공적으로 완료된 이전 단계의 작업들을 거꾸로 되돌리는(undo) 작업을 실행**하는 것을 의미합니다.

Spring의 `@Transactional`과 같은 로컬 트랜잭션은 단일 데이터베이스에만 적용됩니다. 따라서 데이터베이스 변경과 S3 파일 업로드를 함께 처리하는 작업에서 DB 커밋이 실패하면, 이미 S3에 업로드된 파일은 그대로 남아 데이터 불일치가 발생합니다. 보상 트랜잭션은 이러한 문제를 해결하기 위해 애플리케이션 레벨에서 직접 "실행 취소" 로직을 구현하는 것입니다.

- **`파일 이동(move)`**의 보상 트랜잭션: **`파일을 원래 위치로 다시 이동(move back)`**
- **`파일 삭제(delete)`**의 보상 트랜잭션: **`삭제된 파일을 복구(restore)`**

## 2. 구현 전략

핵심 전략은 다음과 같습니다.

1.  **수동 트랜잭션 관리**: `@Transactional` 어노테이션을 제거하고, `PlatformTransactionManager`를 사용하여 DB 트랜잭션의 시작, 커밋, 롤백 시점을 직접 제어합니다.
2.  **외부 시스템 작업 우선 처리**: 실패 시 롤백 비용이 큰 외부 시스템(S3 등)과의 연동을 먼저 수행합니다.
3.  **보상 로직 실행**: DB 트랜잭션이 실패하여 롤백될 경우, `catch` 블록에서 이미 성공한 외부 시스템 작업에 대한 보상 로직을 명시적으로 호출합니다.

## 3. 코드 예시

### 3.1. `ProductImageManager` 인터페이스 확장

보상 로직을 위한 메서드를 인터페이스에 추가하여 역할을 명확히 합니다.

```java
public interface ProductImageManager {
    // 기존 메서드
    List<ProductImage> finalizeImages(Long productId, List<ProductImage> tempImages);
    void deleteImages(List<ProductImage> imagesToDelete);

    // 보상 트랜잭션 메서드 추가
    void compensateFinalizeImages(List<ProductImage> finalizedImages);
    void compensateDeleteImages(List<ProductImage> deletedImages);
}
```

### 3.2. `S3ProductImageManager` 보상 로직 구현

`product-infra` 모듈의 구현체에 실제 보상 로직을 작성합니다.

```java
@Component
public class S3ProductImageManager implements ProductImageManager {
    // ... (기존 코드)

    @Override
    public void compensateFinalizeImages(List<ProductImage> finalizedImages) {
        // finalizeImages에서 수행한 'move' 작업을 되돌리는 로직
        // 즉, 최종 경로(final key)에 있는 파일을 다시 임시 경로(temp key)로 이동시킵니다.
        log.warn("S3 이미지 이동 작업이 보상 처리되었습니다.");
    }

    @Override
    public void compensateDeleteImages(List<ProductImage> deletedImages) {
        // deleteImages에서 수행한 'delete' 작업을 되돌리는 로직
        // (예: S3 버전 관리를 사용하여 파일 복원)
        log.warn("S3 이미지 삭제 작업이 보상 처리되었습니다.");
    }
}
```

### 3.3. `ProductUpdateUseCase` 최종 구현

`UseCase`에서는 `try-catch`를 통해 전체 흐름을 제어하고, 실패 시 보상 매니저를 호출합니다.

```java
@Service
@RequiredArgsConstructor
public class ProductUpdateUseCase {

    private final ProductRepository productRepository;
    private final ProductImageManager productImageManager;
    private final PlatformTransactionManager transactionManager;

    public Product updateProduct(ProductUpdateCommand command) {
        // 1. S3 작업을 먼저 수행하고, 성공 결과물을 변수에 저장
        List<ProductImage> finalizedImages = null;
        List<ProductImage> imagesToRemove = null;

        try {
            if (!CollectionUtils.isEmpty(command.imagesToAdd())) {
                finalizedImages = productImageManager.finalizeImages(command.productId(), command.imagesToAdd());
            }
            if (!CollectionUtils.isEmpty(command.imagesToDelete())) {
                imagesToRemove = productRepository.findImagesByUrls(command.productId(), command.imagesToDelete());
                productImageManager.deleteImages(imagesToRemove);
            }
        } catch (Exception s3Exception) {
            // S3 작업 중 실패 시, 이미 성공한 작업이 있다면 보상 처리
            if (finalizedImages != null) {
                productImageManager.compensateFinalizeImages(finalizedImages);
            }
            throw new RuntimeException("S3 작업 실패. 작업을 롤백합니다.", s3Exception);
        }

        // 2. 데이터베이스 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Product product = findProductById(command.productId());
            // ... 도메인 객체 상태 변경 ...
            
            if (finalizedImages != null) {
                product.addImages(finalizedImages);
            }
            if (imagesToRemove != null) {
                product.removeImages(imagesToRemove);
            }

            Product savedProduct = productRepository.save(product);
            
            // DB 작업 성공 시 커밋
            transactionManager.commit(status);
            return savedProduct;

        } catch (Exception dbException) {
            // DB 작업 실패 시 롤백
            transactionManager.rollback(status);

            // 그리고 이미 성공한 S3 작업들에 대한 보상 트랜잭션 실행
            if (finalizedImages != null) {
                productImageManager.compensateFinalizeImages(finalizedImages);
            }
            if (imagesToRemove != null) {
                productImageManager.compensateDeleteImages(imagesToRemove);
            }
            
            throw new RuntimeException("DB 작업 실패. S3 변경사항에 대한 보상 트랜잭션이 실행되었습니다.", dbException);
        }
    }
}
```

## 4. 기대 효과

- **데이터 정합성 향상**: 여러 시스템에 걸친 작업의 정합성을 보장할 수 있습니다.
- **명확한 책임 분리**: `UseCase`는 비즈니스 흐름 제어, `Manager` 구현체는 인프라 제어 및 보상 책임으로 역할이 명확해집니다.
- **안정성**: 특정 단계의 실패가 전체 시스템에 미치는 영향을 최소화하고, 예측 가능한 방식으로 실패를 처리할 수 있습니다.
