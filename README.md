# Commerce — MSA 커머스 플랫폼

고트래픽 시나리오를 가정한 MSA 기반 커머스 플랫폼. 주문·재고·결제 등 도메인별 트래픽 패턴 차이를 고려해 서비스를 분리하고, AWS ECS Auto-scaling 기반 인프라를 설계·구축했습니다.

> 패스트캠퍼스 InnerCircle 백엔드 4기 팀 프로젝트 (2025.07 ~ 2025.09)

---

## 서비스 구조

```
Client
  └── Gateway (인증·Rate Limiting·라우팅)
        ├── Member Service    — 회원·인증·권한 관리
        ├── Order Service     — 주문·결제
        ├── Product Service   — 상품 등록·조회
        ├── Search Service    — Elasticsearch 기반 상품 검색
        └── Review Service    — 리뷰
```

| 서비스 | 역할 | 담당 |
|--------|------|------|
| [Gateway](./infra/gateway) | JWT 인증 · Token Bucket Rate Limiting · 분산 라우팅 | 노성웅 |
| [Member](./service/member) | 회원 관리 · JWT 인증 · RBAC 권한 체계 | 노성웅 |
| [Order](./service/order) | 주문·결제 흐름 | 박소영 |
| [Product](./service/product-api) | 상품 등록·조회 | 황인웅 |
| [Search](./service/search) | Elasticsearch 상품 검색 | 이상혁 |
| [Review](./service/review) | 리뷰 관리 | 송철현 |
| [Eureka](./infra/eureka-server) | 서비스 디스커버리 | 노성웅 |
| [Config Server](./infra/config-server) | 중앙 설정 관리 | 노성웅 |

---

## 주요 기술 결정

### 인프라 — AWS ECS 기반 MSA
도메인별 트래픽 집중 패턴이 달라 모놀리식은 전체를 스케일링해야 하는 비효율 발생. K8s는 관리 복잡도가 높고, AWS ECS는 관리형 오케스트레이션으로 Auto-scaling 구성이 단순해 MSA + ECS 조합 선택.

### Gateway — Token Bucket Rate Limiting
스파이크 트래픽 시 다운스트림 서비스 과부하 위험. 고정 윈도우·슬라이딩 윈도우는 순간 버스트를 허용하지 못함. 토큰 버킷은 일정 버스트를 허용하면서 평균 속도를 제한하고 Redis 연동 구현이 단순해 선택.

### Member — Hexagonal Architecture + CQRS
외부 의존성(DB·JWT·암호화)을 Port/Adapter로 분리해 도메인 로직 독립성 확보. Command/Query 분리로 읽기·쓰기 최적화.

### 인증 — JWT 무상태 인증 + Gateway 중앙 처리
MSA 환경에서 각 서비스마다 인증 로직 중복 시 변경 시 전 서비스 수정 필요. JWT 무상태 인증을 Gateway에서 일괄 처리해 서비스 간 결합도 완화.

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5, Spring Cloud (Gateway, Eureka, Config) |
| Database | PostgreSQL, Redis |
| Infra | AWS ECS, GitHub Actions |
| Observability | Zipkin, Micrometer, Grafana |
| Test | JUnit5, Testcontainers |

---

## 로컬 실행

```bash
# 인프라 서비스 먼저 기동
cd infra/eureka-server && ./gradlew bootRun &
cd infra/config-server && ./gradlew bootRun &
cd infra/gateway && ./gradlew bootRun &

# Member 서비스
cd service/member && ./gradlew bootRun
```

---

## 관련 문서

- [브랜치 전략](./docs/git-branch-strategy.md)
- [상품 생성 시퀀스](./docs/product_create_sequence_diagram.md)
- [검색 서비스 설계](./docs/search-service-enhancement.md)
- [Snowflake ID 설계](./docs/snowflake.md)
