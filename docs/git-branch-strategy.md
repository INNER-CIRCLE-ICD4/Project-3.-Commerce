# Git 브랜치 전략 가이드

## 📋 개요

이 문서는 우리 이커머스 프로젝트의 Git 브랜치 전략을 정의합니다. 소규모 팀의 빠른 개발과 배포를 위해 **GitHub Flow**를 채택합니다.

## 🎯 브랜치 전략: GitHub Flow

GitHub Flow는 단순하고 효과적인 브랜치 전략으로, 지속적 배포(CD)에 최적화되어 있습니다.

### 핵심 원칙
- **단순함**: main 브랜치와 feature 브랜치만 사용
- **빠른 배포**: 기능 완성 즉시 배포 가능
- **지속적 통합**: 모든 변경사항은 즉시 main에 통합

## 🌳 브랜치 구조

### 1. `main` 브랜치
- **역할**: 프로덕션 배포 코드
- **특징**: 
  - 항상 배포 가능한 상태 유지
  - 모든 커밋은 프로덕션 준비 완료 상태
  - 직접 푸시 금지 (PR 필수)

### 2. `Feature` 브랜치
- **역할**: 새 기능, 버그 수정, 실험
- **명명 규칙**: 
  - `이슈번호-설명적인-브랜치명`
  ```
  123-add-shopping-cart
  124-fix-payment-error
  ```

## 📊 워크플로우 다이어그램

```
main ──────────────────────────────────────────────────────────
     ↑              ↑              ↑              ↑
     │              │              │              │
     │              │              │              │
123-add-      124-fix-       125-update-    126-improve-
shopping-     payment-        product-       search-
cart          error          catalog        performance
```

## 🔄 개발 워크플로우

### 1️⃣ 브랜치 생성
```bash
# main에서 항상 시작
git checkout main
git pull origin main
git checkout -b 123-add-shopping-cart
```

### 2️⃣ 개발 진행
```bash
# 작업 및 커밋
git add .
git commit -m "feat: 장바구니 상품 추가 기능 구현"

# 추가 커밋들...
git commit -m "test: 장바구니 서비스 단위 테스트 추가"
git commit -m "docs: 장바구니 API 문서 업데이트"
```

### 3️⃣ 원격 저장소 푸시
```bash
git push origin 123-add-shopping-cart
```

### 4️⃣ Pull Request 생성
- GitHub에서 PR 생성
- 명확한 설명과 테스트 결과 포함
- 리뷰어 지정

### 5️⃣ 코드 리뷰 & 수정
```bash
# 리뷰 피드백 반영
git add .
git commit -m "refactor: 리뷰 피드백 반영 - 에러 처리 개선"
git push origin 123-add-shopping-cart
```

### 6️⃣ 병합 & 배포
- 리뷰 승인 후 main에 병합
- CI/CD 파이프라인 자동 실행
- 프로덕션 배포

### 7️⃣ 정리
```bash
# 로컬 브랜치 삭제
git checkout main
git pull origin main
git branch -d 123-add-shopping-cart
```

## 🚨 긴급 수정 (Hotfix)

GitHub Flow에서는 별도의 hotfix 브랜치가 없습니다. 모든 수정은 동일한 프로세스를 따릅니다.

```bash
# 긴급 수정도 동일한 플로우
git checkout main
git pull origin main
git checkout -b 127-urgent-fix-payment

# 수정 작업
git add .
git commit -m "fix: 결제 오류 긴급 수정"
git push origin 127-urgent-fix-payment

# PR 생성 시 "urgent" 라벨 추가
# 빠른 리뷰와 병합
```

## 📝 커밋 메시지 컨벤션

### 형식
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅, 세미콜론 누락 등
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드 추가
- `chore`: 빌드 업무, 패키지 매니저 수정 등

### 예시
```
feat(order): 주문 취소 기능 추가

사용자가 배송 전 주문을 취소할 수 있는 기능 구현
- 주문 상태 확인 로직 추가
- 결제 취소 API 연동
- 재고 복구 처리

Resolves: #123
```

## 🚀 Pull Request 가이드

### PR 제목 작성
- 이슈 번호 포함: `[#123] 장바구니 기능 추가`
- 명확한 변경사항 설명

### PR 템플릿
```markdown
## 📋 작업 내용
<!-- 무엇을 변경했는지 간단히 설명 -->

## 🔗 관련 이슈
Closes #123

## 📸 스크린샷
<!-- UI 변경사항이 있다면 스크린샷 첨부 -->

## ✅ 체크리스트
- [ ] 테스트 코드 작성
- [ ] 모든 테스트 통과
- [ ] 코드 리뷰 요청
- [ ] 문서 업데이트 (필요시)
```

### PR 크기 관리
- **이상적**: ~200줄 변경
- **최대**: ~400줄 변경
- 큰 기능은 여러 PR로 분할

## 🛡️ 브랜치 보호 규칙

### `main` 브랜치
- ✅ 직접 푸시 금지
- ✅ PR 필수
- ✅ 최소 1명 리뷰어 승인
- ✅ CI 테스트 통과
- ✅ 최신 main과 동기화

## 💡 Best Practices

### DO ✅
1. **작은 단위로 자주 커밋**: 리뷰와 디버깅이 쉬워짐
2. **main 최신 상태 유지**: 매일 main pull 받기
3. **명확한 브랜치명**: 작업 내용이 바로 이해되도록
4. **빠른 PR 병합**: 24시간 내 리뷰 및 병합 목표
5. **지속적 배포**: 병합 즉시 배포 가능한 상태 유지

### DON'T ❌
1. **장기간 브랜치 유지**: 3일 이상 된 브랜치는 재검토
2. **거대한 PR**: 리뷰가 어려운 대규모 변경 지양
3. **불명확한 커밋**: "fix", "update" 같은 모호한 메시지
4. **테스트 스킵**: 모든 코드는 테스트 필수
