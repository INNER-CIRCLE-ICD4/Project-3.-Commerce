# Config Server

## 📋 개요

Spring Cloud Config Server로 **중앙화된 설정 관리**를 제공하는 서비스입니다. 모든 마이크로서비스의 설정 파일을 Git 저장소에서 관리하고, 런타임에 동적으로 설정을 제공합니다.

---

## 🚀 로컬 실행 방법

### ⚠️ 필수 환경변수 설정

**JASYPT 암호화를 위한 환경변수 설정이 반드시 필요합니다:**

```bash
export JASYPT_ENCRYPTOR_PASSWORD=commerce
```

### 실행 명령어

```bash
# 1. Config Server 디렉토리로 이동
cd infra/config-server

# 2. 환경변수와 함께 실행
JASYPT_ENCRYPTOR_PASSWORD=commerce ./gradlew bootRun

# 또는 환경변수 export 후 실행
export JASYPT_ENCRYPTOR_PASSWORD=commerce
./gradlew bootRun
```

### 서비스 확인

```bash
# Config Server 헬스체크
curl http://localhost:9000/actuator/health

# 응답 예시:
# {"status":"UP"}
```

---

## 🔧 주요 기능

### 1. 중앙화된 설정 관리
- **Git 기반**: 모든 설정을 Git 저장소에서 버전 관리
- **환경별 설정**: dev, staging, prod 환경별 설정 파일 제공
- **실시간 갱신**: `/actuator/refresh` 엔드포인트로 설정 리로드

### 2. 보안 강화
- **JASYPT 암호화**: 민감한 정보(DB 패스워드, API Key 등) 암호화 저장
- **Spring Security**: 설정 접근에 대한 인증/인가 처리
- **Git 인증**: Private Repository 접근을 위한 Git 계정 연동

### 3. 서비스 연동
- **동적 설정 로드**: 마이크로서비스 시작 시 자동으로 설정 다운로드
- **설정 우선순위**: application.yml < application-{profile}.yml < Git 설정
- **Fallback 지원**: Config Server 장애 시 로컬 설정으로 대체

---

## 📁 설정 파일 구조

### Git 저장소 구조 (예시)
```
config-repo/
├── application.yml                 # 공통 설정
├── application-local.yml           # 로컬 환경 설정
├── application-prod.yml            # 운영 환경 설정
├── gateway.yml                     # Gateway 서비스 설정
├── gateway-local.yml              # Gateway 로컬 설정
├── member-service.yml             # Member 서비스 설정
├── member-service-local.yml       # Member 로컬 설정
└── order-service.yml              # Order 서비스 설정
```

### 설정 우선순위
1. **Git의 {service-name}-{profile}.yml** (최고 우선순위)
2. **Git의 {service-name}.yml**
3. **Git의 application-{profile}.yml**
4. **Git의 application.yml**
5. **로컬의 application-{profile}.yml**
6. **로컬의 application.yml** (최저 우선순위)

---

## 🔐 JASYPT 암호화 사용법

### 1. 암호화된 값 생성
```bash
# JASYPT CLI 또는 테스트 코드로 암호화
# 예: "mysecretpassword" → "ENC(HNFZ9+JqltsDY/edbkhO+g...)"
```

### 2. 설정 파일에 암호화된 값 사용
```yaml
# Git 저장소의 설정 파일
datasource:
  url: jdbc:postgresql://localhost:5432/commerce
  username: ENC(DDubtfkAGniOPQ4W4zFVZMsRQOw1N67bfMfzFfrxHK0=)
  password: ENC(HNFZ9+JqltsDY/edbkhO+gf3EAKFKnhRihNTHfo41PcxF1ncs1VSs1McVR2vYLUVWlTI8WLtSMQ=)

jwt:
  secret: ENC(XYZ123abc456def789...)
```

### 3. 클라이언트 서비스에서 복호화
Config Server에서 설정을 가져올 때 자동으로 복호화되어 제공됩니다.

---

## 🌐 클라이언트 서비스 연동

### 1. 의존성 추가 (클라이언트)
```kotlin
// build.gradle.kts
implementation("org.springframework.cloud:spring-cloud-starter-config")
implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
```

### 2. bootstrap.yml 설정 (클라이언트)
```yaml
spring:
  application:
    name: member-service  # Config Server에서 찾을 설정 파일명
  profiles:
    active: local
  cloud:
    config:
      uri: http://localhost:9000  # Config Server 주소
      fail-fast: true             # Config Server 연결 실패 시 서비스 시작 중단
      retry:
        initial-interval: 1000
        max-attempts: 6
```

### 3. 설정 갱신 (클라이언트)
```bash
# 특정 서비스의 설정 갱신
curl -X POST http://localhost:8080/actuator/refresh

# 모든 서비스 설정 갱신 (Spring Cloud Bus 사용 시)
curl -X POST http://localhost:9000/actuator/bus-refresh
```

---

## 🧪 테스트 방법

### 1. Config Server 설정 확인
```bash
# 특정 서비스의 설정 조회
curl http://localhost:9000/{service-name}/{profile}

# 예시: Member Service의 로컬 설정 조회
curl http://localhost:9000/member-service/local

# 예시: Gateway의 운영 설정 조회  
curl http://localhost:9000/gateway/prod
```

### 2. 설정 적용 확인
```bash
# Member Service에서 설정 확인
curl http://localhost:8080/actuator/configprops

# 환경변수 확인
curl http://localhost:8080/actuator/env
```

### 3. JASYPT 암호화 테스트
```bash
# JasyptConfigTest 실행으로 암호화/복호화 테스트
./gradlew :infra:config-server:test --tests JasyptConfigTest
```

---

## 🚨 문제 해결

### 1. JASYPT_ENCRYPTOR_PASSWORD 누락
**에러:**
```
Error creating bean with name 'encryptablePropertySourceConverter'
```

**해결:**
```bash
export JASYPT_ENCRYPTOR_PASSWORD=commerce
```

### 2. Git 인증 실패
**에러:**
```
Cannot clone or checkout repository
```

**해결:**
1. Git 저장소 접근 권한 확인
2. username/password 또는 SSH Key 설정 확인
3. application.yml의 암호화된 Git 설정 확인

### 3. 클라이언트 연결 실패
**에러:**
```
Could not locate PropertySource: I/O error on GET request
```

**해결:**
1. Config Server가 실행 중인지 확인 (`localhost:9000`)
2. 클라이언트의 `spring.cloud.config.uri` 확인
3. 네트워크 연결 상태 확인

---

## 📊 모니터링

### Actuator 엔드포인트
```bash
# 헬스체크
curl http://localhost:9000/actuator/health

# 환경 정보
curl http://localhost:9000/actuator/env

# Git 정보 (Config Server 전용)
curl http://localhost:9000/actuator/configserver
```

### 로그 확인
```bash
# Config Server 로그 모니터링
tail -f logs/spring.log | grep -i "config\|git\|jasypt"

# Git 동기화 로그
grep "Fetching config from server" logs/spring.log
```

---

## 🚀 운영 가이드

### 1. 설정 변경 프로세스
1. **Git 저장소**에서 설정 파일 수정
2. **Commit & Push**로 변경사항 반영
3. **Config Server**에서 자동으로 최신 설정 동기화
4. **클라이언트 서비스**에서 `/actuator/refresh` 호출

### 2. 보안 권장사항
- **JASYPT 비밀번호**: 운영환경에서는 강력한 비밀번호 사용
- **Git 저장소**: Private Repository 사용 권장
- **네트워크**: Config Server를 내부 네트워크에만 노출
- **인증**: Spring Security로 접근 제한

### 3. 백업 및 복구
- **Git 저장소**: 정기적인 백업 수행
- **설정 히스토리**: Git 커밋 히스토리로 변경 추적
- **Fallback**: 로컬 설정 파일을 항상 유지

---

## 📚 참고 자료

- [Spring Cloud Config Documentation](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)
- [JASYPT Spring Boot Guide](https://github.com/ulisesbocchio/jasypt-spring-boot)
- [Git Repository 설정 가이드](https://spring.io/guides/gs/centralized-configuration/)

---

**Port**: 9000  
**Profile**: local, prod  
**Required ENV**: `JASYPT_ENCRYPTOR_PASSWORD=commerce`  
**Last Updated**: 2025-08-13

