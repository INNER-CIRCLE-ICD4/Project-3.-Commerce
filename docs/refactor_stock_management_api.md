# 재고 관리 기능 리팩토링 및 API 역할 분리 가이드

## 1. 배경 (As-Is)

현재 프로젝트의 재고 증감 기능은 `product-admin` 모듈에 구현되어 있습니다. 이 기능은 본래 클라이언트의 주문 생성, 주문 취소 등 시스템 이벤트에 따라 재고를 변경하기 위한 목적으로 설계되었습니다.

하지만 `product-admin` 모듈은 판매자 또는 시스템 관리자가 직접 상품 정보를 관리하는 컨텍스트에 더 적합합니다. 시스템 이벤트에 의한 재고 변경 로직이 관리자용 모듈에 포함되어 있어, 역할과 책임이 불분명한 상태입니다.

- **문제점**:
  - `product-admin`: 관리자용 기능과 시스템용 기능이 혼재
  - `product-api`: 클라이언트용 API가 정의되어 있지 않아 확장성 저하
  - 헥사고날 아키텍처의 '포트와 어댑터' 원칙에 따라 역할 분리가 필요

## 2. 개선 목표 (To-Be)

각 모듈의 역할을 명확히 분리하여 아키텍처를 개선하고 유지보수성을 높입니다.

- **`product-api` 모듈**:
  - **역할**: 클라이언트(다른 서비스)의 요청에 따른 시스템 이벤트를 처리합니다.
  - **기능**: 주문/결제 시스템의 요청에 따라 재고를 **증가(increase)** 또는 **감소(decrease)** 시킵니다. (예: 주문 취소 시 재고 복구)

- **`product-admin` 모듈**:
  - **역할**: 판매자 또는 관리자가 직접 데이터를 관리하는 기능을 제공합니다.
  - **기능**: 창고 실사 등 관리 목적을 위해 재고 수량을 **절대값으로 조정(adjust)**합니다.

## 3. 리팩토링 계획 (Hexagonal Architecture 기반)

### 1단계: 도메인 로직 강화 (`product-core`)

모든 재고 관련 비즈니스 로직을 `Product` 도메인 엔티티로 중앙화합니다.

1.  `Product` 엔티티에 기존의 `increaseStock(long quantity)` 및 `decreaseStock(long quantity)` 메서드를 검토합니다.
2.  재고의 음수 방지 등 핵심 비즈니스 규칙이 도메인 내에 캡슐화되도록 보장합니다.
3.  `product-admin`을 위한 재고 절대값 조정 메서드 `adjustStock(long quantity)`를 `Product` 엔티티에 새로 추가합니다.

### 2단계: 재고 증감 기능 이전 (`product-api`로 이동)

`product-admin`의 기존 재고 증감 기능을 `product-api`로 이전합니다.

1.  **UseCase 이전 및 생성**:
    - `product-admin`의 재고 증감 UseCase 로직을 `product-api` 모듈로 이동하여 `ProductStockIncreaseUseCase`, `ProductStockDecreaseUseCase`를 생성합니다.
2.  **Controller 구현**:
    - `product-api`에 `ProductStockApiController`를 생성합니다.
    - 아래와 같은 시스템 간 연동을 위한 API 엔드포인트를 정의합니다.
      - `POST /api/v1/products/{productId}/increase-stock`
      - `POST /api/v1/products/{productId}/decrease-stock`

### 3단계: 재고 조정 기능 구현 (`product-admin` 리팩토링)

`product-admin` 모듈을 리팩토링하여 관리자용 기능만 남깁니다.

1.  **기존 코드 제거**: `product-admin`에서 `product-api`로 이전된 재고 증감 관련 UseCase 및 Controller 코드를 삭제합니다.
2.  **신규 UseCase 구현**:
    - `ProductStockAdjustUseCase`를 `product-admin`에 생성합니다. 이 UseCase는 1단계에서 만든 `product.adjustStock()` 도메인 메서드를 호출합니다.
3.  **신규 Controller 구현**:
    - `product-admin`의 `ProductManageController`에 관리자용 API 엔드포인트를 정의합니다.
      - `PUT /admin/v1/products/{productId}/stock`
    - 이 API는 요청 Body를 통해 재고의 절대값을 받아 처리합니다.

## 4. API 엔드포인트 변경 요약

| 구분 | 변경 전 (As-Is) | 변경 후 (To-Be) | 담당 모듈 | 주 사용처 |
| --- | --- | --- | --- | --- |
| **재고 증감** | `POST /admin/v1/...` | `POST /api/v1/.../increase-stock` | `product-api` | 주문/결제 서비스 |
| **재고 조정** | (없음) | `PUT /admin/v1/.../stock` | `product-admin` | 판매자/관리자 |

## 5. 기대 효과

- **명확한 역할과 책임 분리**: 각 모듈이 컨텍스트에 맞는 기능만 수행합니다.
- **유지보수성 향상**: 코드의 응집도는 높아지고 결합도는 낮아져 변경이 용이해집니다.
- **확장성 개선**: 향후 클라이언트용 API 또는 관리자용 기능을 추가할 때 명확한 경계를 가지고 확장할 수 있습니다.
