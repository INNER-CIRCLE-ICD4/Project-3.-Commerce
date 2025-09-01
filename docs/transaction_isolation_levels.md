### PostgreSQL vs MySQL: 트랜잭션 격리 수준 비교 (`READ COMMITTED` vs `REPEATABLE READ`)

트랜잭션 격리 수준은 여러 트랜잭션이 동시에 실행될 때 서로에게 얼마나 영향을 미칠지를 결정하는 설정입니다. `READ COMMITTED`와 `REPEATABLE READ`는 가장 널리 사용되는 두 가지 격리 수준입니다.

#### 1. 기본 개념

*   **`READ COMMITTED` (커밋된 읽기)**
    *   **동작 방식**: 트랜잭션 내에서 쿼리를 실행할 때마다 **가장 최근에 커밋된** 데이터만 읽습니다.
    *   **발생 가능한 현상**:
        *   **Non-Repeatable Read (반복 불가능한 읽기)**: 한 트랜잭션 내에서 같은 데이터를 두 번 조회했을 때, 그 사이에 다른 트랜잭션이 데이터를 수정하고 커밋하면 서로 다른 결과가 반환될 수 있습니다.
    *   **장점**: 정합성과 동시성 사이의 균형이 좋아서 많은 데이터베이스(PostgreSQL, Oracle 등)에서 **기본 격리 수준**으로 사용됩니다.

*   **`REPEATABLE READ` (반복 가능한 읽기)**
    *   **동작 방식**: 트랜잭션이 시작될 때의 데이터 스냅샷을 만들어, 트랜잭션이 끝날 때까지 **동일한 스냅샷**의 데이터만 읽습니다.
    *   **발생 가능한 현상**:
        *   **Phantom Read (유령 읽기)**: 한 트랜잭션 내에서 특정 범위의 데이터를 조회했는데, 그 사이에 다른 트랜잭션이 새로운 데이터를 추가하고 커밋하면, 같은 쿼리를 다시 실행했을 때 이전에 없던 "유령" 같은 데이터가 나타날 수 있습니다.
    *   **장점**: 트랜잭션 내내 데이터 일관성이 보장되므로 Non-Repeatable Read를 방지합니다. **MySQL(InnoDB)의 기본 격리 수준**입니다.

#### 2. PostgreSQL과 MySQL의 주요 차이점

두 데이터베이스 모두 표준 SQL을 따르지만, `REPEATABLE READ`를 구현하는 방식에 중요한 차이가 있습니다.

| 특징 | `READ COMMITTED` | `REPEATABLE READ` |
| :--- | :--- | :--- |
| **기본 격리 수준** | **PostgreSQL** | **MySQL (InnoDB)** |
| **Non-Repeatable Read** | 발생 가능 (양쪽 모두) | 방지됨 (양쪽 모두) |
| **Phantom Read** | 발생 가능 (양쪽 모두) | **MySQL**: **대부분 방지됨** (Gap Lock 사용)<br>**PostgreSQL**: **발생 가능** (대신 직렬화 오류 발생 가능) |
| **구현 방식 (MVCC)** | 쿼리마다 새로운 스냅샷 생성 | 트랜잭션 시작 시점의 스냅샷 사용 |

*   **MySQL (InnoDB)의 `REPEATABLE READ`**:
    *   MVCC(다중 버전 동시성 제어)와 함께 **Gap Lock** 및 **Next-Key Lock**이라는 잠금 메커니즘을 사용합니다.
    *   이는 특정 범위의 데이터에 새로운 데이터가 삽입되는 것을 막아주므로, 표준에서 정의한 `REPEATABLE READ` 수준보다 강력하게 **Phantom Read를 대부분 방지**합니다. 사실상 `SERIALIZABLE`에 가까운 격리성을 제공합니다.

*   **PostgreSQL의 `REPEATABLE READ`**:
    *   순수한 MVCC 스냅샷 방식으로 동작하며, MySQL처럼 Gap Lock을 사용하지 않습니다.
    *   따라서 Phantom Read가 발생할 수 있습니다. 만약 스냅샷 버전과 현재 데이터베이스 상태 간에 충돌이 감지되면(예: 내가 읽은 데이터가 다른 트랜잭션에 의해 수정/삭제된 후 커밋될 때), PostgreSQL은 트랜잭션을 강제로 실패시키고 **"직렬화 오류(Serialization Failure)"**를 발생시킵니다. 개발자는 이 오류를 잡아서 트랜잭션을 재시도해야 합니다.

#### 3. Spring Boot 및 ORM(JPA/Hibernate)에 미치는 영향

이러한 차이점은 Spring 프레임워크 위에서 동작하는 애플리케이션에 중요한 영향을 미칩니다.

*   **`@Transactional`의 기본 동작**
    *   Spring의 `@Transactional` 어노테이션에서 격리 수준을 명시하지 않으면(`isolation = ...`), 데이터베이스의 **기본 격리 수준**을 따릅니다.
    *   **치명적인 문제**: 동일한 Spring Boot 코드가 **PostgreSQL에 연결되면 `READ COMMITTED`**로, **MySQL에 연결되면 `REPEATABLE READ`**로 동작합니다. 이는 애플리케이션의 동작을 예측하기 어렵게 만드는 원인이 됩니다.

*   **ORM(JPA/Hibernate) 관점에서의 영향**
    1.  **`READ COMMITTED` (PostgreSQL 기본)**
        *   **영속성 컨텍스트(1차 캐시)와 불일치**: 트랜잭션 내에서 조회한 엔티티가 1차 캐시에 저장됩니다. 하지만 다른 트랜잭션이 데이터를 변경하고 커밋하면, DB의 데이터는 바뀌었지만 1차 캐시는 여전히 이전 데이터를 가지고 있습니다. 이 상태에서 다시 같은 데이터를 조회하면 DB에서 새로 읽어와 Non-Repeatable Read가 발생하며, 1차 캐시와 DB 간의 데이터 불일치로 인해 예기치 않은 동작이 발생할 수 있습니다.
        *   **동시성**: 잠금 범위가 좁아 동시 처리 성능이 일반적으로 더 좋습니다. 대부분의 웹 애플리케이션에 적합합니다.

    2.  **`REPEATABLE READ` (MySQL 기본)**
        *   **데이터 일관성**: 트랜잭션 내내 일관된 데이터 스냅샷을 제공하므로, ORM의 1차 캐시와 데이터베이스 간의 불일치 문제가 거의 없습니다. "조회 -> 비즈니스 로직 처리 -> 수정"과 같은 작업 흐름에서 데이터 정합성을 유지하기에 유리합니다.
        *   **잠재적 데드락**: MySQL의 Gap Lock 때문에 특정 범위에 대한 잠금이 발생하여 다른 트랜잭션의 INSERT 작업을 막을 수 있습니다. 이는 동시성이 중요한 환경에서 데드락(Deadlock)이나 성능 저하의 원인이 될 수 있습니다.

#### 4. 권장 사항 및 결론

1.  **격리 수준 명시**: 데이터베이스 기본값에 의존하지 말고, `@Transactional(isolation = ...)`을 사용해 비즈니스 로직에 필요한 격리 수준을 **코드에 명시적으로 지정**하는 것이 좋습니다.
    ```java
    // 예: 재고 차감과 같이 중요한 데이터 정합성이 필요할 때
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void decreaseStock(Long productId, int quantity) {
        // ...
    }
    ```

2.  **DB 기본값 통일**: 개발/스테이징/운영 환경에서 사용하는 DB가 다르다면, `application.yml` (또는 `properties`)에 격리 수준을 설정하여 동작을 통일하는 것이 안전합니다.
    ```yaml
    spring:
      jpa:
        properties:
          hibernate:
            connection:
              isolation: READ_COMMITTED # 2 (READ_COMMITTED의 정수 값)
    ```

3.  **DB 특성 이해**: MySQL을 사용한다면 `REPEATABLE READ`와 Gap Lock의 특성을, PostgreSQL을 사용한다면 `REPEATABLE READ`에서 발생할 수 있는 "직렬화 오류"와 재시도 처리의 필요성을 이해해야 합니다.

**결론적으로**, `READ COMMITTED`는 대부분의 일반적인 웹 애플리케이션에 적합한 "실용적인" 선택지이며, `REPEATABLE READ`는 트랜잭션 내에서 더 엄격한 데이터 일관성이 필요할 때 사용하는 강력한 도구입니다. 하지만 각 DB의 구현 방식 차이를 이해하지 못하면 예기치 않은 버그의 원인이 될 수 있으므로 주의가 필요합니다.