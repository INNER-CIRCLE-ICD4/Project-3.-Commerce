# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

Search Service는 이커머스 플랫폼의 검색 기능을 담당하는 마이크로서비스입니다. Spring Boot 3.5.3 기반으로 구축되었으며, Java 21을 사용합니다.

## 빌드 및 실행 명령어

### 로컬 실행
```bash
# 프로젝트 루트에서 실행
./gradlew :service:search:bootRun
```

### JAR 빌드 및 실행
```bash
# JAR 빌드
./gradlew :service:search:build

# JAR 실행
java -jar service/search/build/libs/search-0.0.1-SNAPSHOT.jar
```

### Docker 실행
```bash
# Docker 이미지 빌드 (프로젝트 루트에서)
docker build -f service/search/Dockerfile -t commerce-search-service .

# Docker 컨테이너 실행
docker run -p 9002:9002 commerce-search-service
```

### 테스트 실행
```bash
# 전체 테스트
./gradlew :service:search:test

# 특정 테스트 클래스 실행
./gradlew :service:search:test --tests "패키지명.테스트클래스명"
```

## 프로젝트 구조

Search Service는 이커머스 모놀리스 리포지토리의 일부로, 다음과 같은 멀티 모듈 구조를 따릅니다:

```
commerce/
├── service/
│   ├── search/           # 검색 서비스
│   ├── member/          # 회원 서비스
│   ├── product/         # 상품 서비스
│   ├── order/           # 주문 서비스
│   └── review/          # 리뷰 서비스
├── infra/
│   ├── gateway/         # API 게이트웨이
│   ├── eureka-server/   # 서비스 디스커버리
│   └── config-server/   # 중앙 설정 관리
└── common/
    ├── snowflake/       # 분산 ID 생성기
    └── logging/         # 공통 로깅
```

## 주요 설정

- **포트**: 9002
- **애플리케이션 이름**: commerce-search-service
- **JVM 설정** (Docker):
  - 최대 힙: 512MB (-Xmx512m)
  - 초기 힙: 256MB (-Xms256m)
  - GC: G1GC

## 의존성

- Spring Boot Starter Web
- Snowflake ID Generator (공통 모듈)
- Lombok (컴파일 타임)

## 개발 시 주의사항

1. Java 21 버전 사용 필수
2. Gradle 멀티모듈 프로젝트 구조를 따름
3. 공통 모듈 사용 시 `project(":common:모듈명")` 형식으로 의존성 추가
4. PR 작성 시 `.github/PULL_REQUEST_TEMPLATE.md` 템플릿 준수