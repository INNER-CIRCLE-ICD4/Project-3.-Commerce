# Search Service 형태소 분석 및 동의어 처리 구현

## 개요
이 문서는 Commerce 플랫폼의 Search Service에 형태소 분석 및 동의어 처리 기능을 구현한 내용을 정리합니다.

## 구현 내용

### 1. 형태소 분석 기능
- **Nori Analyzer** 사용: 한국어 형태소 분석기
- 적용 필드: `name`, `description`, `brandName`, `categories.name`
- 설정:
  - Tokenizer: `nori_tokenizer` (decompound_mode: mixed)
  - Filters: `nori_part_of_speech`, `lowercase`, `stop`

### 2. 동의어 처리 기능
- **nori_synonym_analyzer** 추가: 형태소 분석 + 동의어 처리
- 동의어 사전 파일: `src/main/resources/synonyms.txt`
- 동의어 예시:
  - 휴대폰, 핸드폰, 스마트폰, 폰
  - 티셔츠, 티, 반팔, 반팔티
  - 신발, 운동화, 스니커즈, 구두

### 3. 자동완성 기능
- **autocomplete_analyzer** 사용
- Edge N-gram 필터 적용 (min_gram: 1, max_gram: 20)
- `name` 필드에 `name.autocomplete` 서브필드 추가

### 4. 검색 기능 개선
- **Multi-Match Query**: 여러 필드에서 동시 검색
  - 가중치 적용: name^3, description^2, brandName^2, categories.name
- **Fuzzy Search**: 오타 허용 검색 (AUTO 설정)
- **필터링**: 카테고리, 브랜드, 가격 범위
- **정렬**: 가격순, 최신순, 관련도순

## 변경된 파일

### 1. Elasticsearch 설정
- `product-settings.json`: analyzer 및 filter 설정 추가

### 2. 도메인 모델
- `Product.java`: 
  - 형태소 분석기 변경 (nori_analyzer → nori_synonym_analyzer)
  - 자동완성 필드 추가 (@MultiField)

### 3. Repository 구현
- `ProductSearchRepositoryImpl.java`:
  - Elasticsearch Java API Client 사용
  - BoolQuery로 복합 검색 구현
  - 동적 필터링 및 정렬 구현

### 4. 테스트 코드
- `ProductSearchRepositoryImplTest.java`:
  - 형태소 분석 테스트
  - 동의어 검색 테스트
  - 자동완성 테스트
  - 필터링 및 정렬 테스트

## 사용 예시

### 1. 동의어 검색
```java
// "핸드폰"으로 검색하면 "스마트폰" 상품도 검색됨
ProductSearchRequest request = ProductSearchRequest.builder()
    .keyword("핸드폰")
    .build();
```

### 2. 복합 검색
```java
// 키워드 + 카테고리 + 가격 필터
ProductSearchRequest request = ProductSearchRequest.builder()
    .keyword("운동화")
    .categoryIds(List.of(2L))
    .maxPrice(new BigDecimal("100000"))
    .sortType(ProductSearchRequest.SortType.PRICE_ASC)
    .build();
```

### 3. 자동완성
```java
// "스마트"로 시작하는 상품명 추천
List<String> suggestions = searchService.getAutocompleteSuggestions("스마트", 5);
```

## 주의사항

1. **인덱스 재생성 필요**: 매핑 변경으로 인해 기존 인덱스 재생성 필요
2. **동의어 파일 경로**: Elasticsearch 설정에서 `synonyms.txt` 파일 경로 확인 필요
3. **Nori 플러그인**: Elasticsearch에 analysis-nori 플러그인 설치 필요

## 향후 개선사항

1. **동적 동의어 관리**: API를 통한 동의어 추가/수정/삭제
2. **검색 로그 분석**: 사용자 검색 패턴 분석 및 동의어 자동 추천
3. **성능 최적화**: 대용량 데이터 처리를 위한 샤딩 전략
4. **인기도 정렬**: 조회수/판매량 기반 정렬 기능 추가