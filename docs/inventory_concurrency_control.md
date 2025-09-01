# 상품 재고 변경 동시성 제어 (낙관적 락)

## 시퀀스 다이어그램

```mermaid
sequenceDiagram
    actor 사용자 A
    actor 사용자 B
    participant Controller as ProductInventoryAdminController
    participant UseCase as ProductInventoryUpdateUseCase
    participant Product as Product (Domain)
    database Database

    title 상품 재고 변경 동시성 제어 (낙관적 락)

    %% --- 1. 사용자 A와 B가 동시에 재고가 10개(version=1)인 상품 조회 ---
    Note over 사용자 A, Database: 초기 상태: 재고=10, version=1

    %% --- 2. 사용자 A의 재고 감소 요청 (성공 시나리오) ---
    사용자 A->>Controller: PATCH /products/{id}/inventory (재고 -2 요청)
    Controller->>UseCase: updateStock(productId, quantity: -2)
    UseCase->>Database: 1. findById(id)
    Database-->>UseCase: Product(stock=10, version=1) 반환
    UseCase->>Product: 2. decreaseStock(2)
    Product-->>UseCase: 재고 8로 변경
    
    %% --- 3. 그 사이 사용자 B의 재고 감소 요청 ---
    사용자 B->>Controller: PATCH /products/{id}/inventory (재고 -3 요청)
    Controller->>UseCase: updateStock(productId, quantity: -3)
    UseCase->>Database: 1. findById(id)
    Note right of UseCase: 아직 사용자 A의 트랜잭션이 커밋되지 않아<br/> 똑같이 version=1인 데이터를 읽음
    Database-->>UseCase: Product(stock=10, version=1) 반환
    UseCase->>Product: 2. decreaseStock(3)
    Product-->>UseCase: 재고 7로 변경

    %% --- 4. 사용자 A의 트랜잭션 커밋 ---
    UseCase->>Database: 3. save(Product)
    note right of Database: UPDATE products SET stock=8, version=2<br/>WHERE id={id} AND version=1
    Database-->>UseCase: 업데이트 성공 (1 row affected)
    UseCase-->>Controller: 성공 응답
    Controller-->>사용자 A: 200 OK (updatedStock: 8)

    %% --- 5. 사용자 B의 트랜잭션 커밋 시도 및 실패 ---
    UseCase->>Database: 3. save(Product)
    note right of Database: UPDATE products SET stock=7, version=2<br/>WHERE id={id} AND version=1
    
    alt 충돌 발생!
        Database-->>UseCase: 업데이트 실패 (0 rows affected)
        Note right of UseCase: DB의 version은 2, 요청의 version은 1 -> 불일치
        UseCase-xUseCase: OptimisticLockException 발생!
        UseCase-->>Controller: 예외 전달 (StockConflictException)
        Controller-->>사용자 B: 409 Conflict (재시도 요청)
    end

```

## 다이어그램 해설

1.  **조회**: 사용자 A와 B가 거의 동시에 같은 상품 정보(`version=1`)를 읽습니다.
2.  **요청 A 처리**: 사용자 A의 요청이 먼저 데이터베이스에 도달하여 `UPDATE`를 시도합니다. `version=1`인 조건을 만족하므로 재고를 8로 변경하고 `version`을 2로 증가시킨 후 성공적으로 커밋됩니다.
3.  **요청 B 처리 및 충돌**: 사용자 B의 요청이 뒤이어 데이터베이스에 `UPDATE`를 시도합니다. 하지만 사용자 B가 알고 있는 `version`은 1인데, 데이터베이스의 `version`은 이미 2로 변경된 상태입니다.
4.  **실패 및 예외**: `UPDATE`문의 `WHERE` 조건(`version=1`)이 맞지 않아 아무 데이터도 수정되지 않습니다. JPA는 이를 감지하고 `OptimisticLockException`을 발생시켜, 사용자 B의 요청이 실패했음을 알립니다.

이 다이어그램은 어떻게 낙관적 락이 두 요청 사이의 경합을 해결하고 데이터의 일관성을 유지하는지를 명확하게 보여줍니다.
