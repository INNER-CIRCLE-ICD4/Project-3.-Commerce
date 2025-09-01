# 이미지 지연 삭제 기능 구현 계획

## 1. 배경 및 목적

현재 시스템에서는 상품 삭제 시 연결된 이미지 파일을 즉시 물리적으로 삭제하고 있습니다. 이 방식은 사용자의 실수나 변심으로 인한 데이터 복구를 어렵게 만듭니다.

본 문서는 상품 삭제 시 이미지 파일을 즉시 삭제하지 않고, **30일의 유예 기간**을 둔 후 삭제하는 '지연 삭제' 기능을 구현하는 계획을 기술합니다. 이를 통해 데이터 복구 가능성을 열어두고 시스템의 안정성을 향상시키는 것을 목표로 합니다.

## 2. 핵심 구현 방안: 스케줄러와 별도 엔티티 활용

이미지 삭제를 '별개의 작업(Task)'으로 간주하고, `ImageDeletionTarget`이라는 별도의 엔티티를 두어 삭제 대상을 관리합니다. 주기적으로 실행되는 스케줄러가 이 테이블을 조회하여 실제 파일 삭제를 수행합니다.

### 주요 장점
- **명확한 책임 분리**: `Product`와 '이미지 삭제' 작업의 책임이 분리됩니다.
- **멱등성 보장**: 처리된 작업은 테이블에서 제거되므로 중복 실행이 원천적으로 방지됩니다.
- **성능 및 확장성**: 작업 테이블만 조회하므로 성능이 우수하며, 향후 복원 기능 등을 추가하기 용이합니다.

## 3. 상세 구현 계획

### 3.1. `ImageDeletionTarget` 엔티티 및 리포지토리 추가

삭제할 이미지의 정보를 담는 엔티티와 이를 처리할 리포지토리를 생성합니다.

- **`ImageDeletionTarget.java`**
  ```java
  @Entity
  @Table(name = "image_deletion_targets")
  public class ImageDeletionTarget {
  
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
  
      @Column(nullable = false, updatable = false)
      private String imageUrl;
  
      @Column(nullable = false, updatable = false)
      private LocalDateTime scheduledDeletionAt; // 삭제 예정 시간
  
      // 생성자, Getter 등
  }
  ```

- **`ImageDeletionTargetRepository.java`**
  ```java
  public interface ImageDeletionTargetRepository extends JpaRepository<ImageDeletionTarget, Long> {
      List<ImageDeletionTarget> findAllByScheduledDeletionAtBefore(LocalDateTime now);
  }
  ```

### 3.2. `ProductDeleteUseCase` 로직 변경

상품 삭제 시, 이벤트를 발행하는 대신 `ImageDeletionTarget` 레코드를 생성하도록 수정합니다.

- **`ProductDeleteUseCase.java` (수정)**
  ```java
  // ...
  // private final ApplicationEventPublisher eventPublisher; // 제거
  private final ImageDeletionTargetRepository imageDeletionTargetRepository; // 추가

  public void deleteProduct(Long productId) {
      Product product = findProductById(productId);

      // 1. 이미지 URL을 '삭제 대기 테이블'에 저장
      if (!CollectionUtils.isEmpty(product.getImages())) {
          LocalDateTime scheduledAt = LocalDateTime.now().plusDays(30); // 30일 후 삭제
          List<ImageDeletionTarget> targets = product.getImages().stream()
                  .map(image -> new ImageDeletionTarget(image.getUrl(), scheduledAt))
                  .collect(Collectors.toList());
          imageDeletionTargetRepository.saveAll(targets);
      }

      // 2. 상품 논리적 삭제
      product.delete();
      productRepository.save(product);
      
      // 3. 이벤트 발행 로직 제거
  }
  // ...
  ```

### 3.3. `ImageDeletionScheduler` 스케줄러 구현

매일 정해진 시간에 실행되어 삭제 예정일이 지난 이미지를 실제로 삭제하는 스케줄러를 구현합니다.

- **`ImageDeletionScheduler.java`**
  ```java
  @Component
  @RequiredArgsConstructor
  public class ImageDeletionScheduler {
  
      private final ImageDeletionTargetRepository imageDeletionTargetRepository;
      private final ImageStorageService imageStorageService; // 실제 파일 삭제 서비스
  
      /**
       * 매일 새벽 4시에 실행
       */
      @Scheduled(cron = "0 0 4 * * *")
      @Transactional
      public void deleteScheduledImages() {
          List<ImageDeletionTarget> targets = imageDeletionTargetRepository.findAllByScheduledDeletionAtBefore(LocalDateTime.now());
          
          for (ImageDeletionTarget target : targets) {
              try {
                  imageStorageService.delete(target.getImageUrl()); // 1. 스토리지에서 삭제
                  imageDeletionTargetRepository.delete(target);     // 2. DB에서 작업 정보 제거
              } catch (Exception e) {
                  // 실패 시 오류 로깅
              }
          }
      }
  }
  ```

## 4. 추가 고려사항

- **설정 분리**: '30일'이라는 삭제 유예 기간은 `application.yml` 등 설정 파일로 분리하여 관리합니다.
- **인덱싱**: `ImageDeletionTarget` 테이블의 `scheduledDeletionAt` 컬럼에 인덱스를 추가하여 조회 성능을 최적화합니다.
- **오류 처리**: 스케줄러 실행 중 특정 이미지 삭제 실패가 다른 작업에 영향을 주지 않도록 예외 처리를 견고하게 구현합니다.
- **상품 복원**: 상품 복원 기능 구현 시, `ImageDeletionTarget` 테이블에서 관련된 삭제 작업을 함께 제거하여 이미지 삭제를 취소하는 로직을 반드시 포함해야 합니다.
