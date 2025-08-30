# PostgreSQL 설정 및 테이블 생성 가이드

## 📋 목차
1. [PostgreSQL 구조 이해](#postgresql-구조-이해)
2. [데이터베이스 및 스키마 설정](#데이터베이스-및-스키마-설정)
3. [사용자 생성 및 권한 설정](#사용자-생성-및-권한-설정)
4. [테이블 생성](#테이블-생성)
5. [권한 문제 해결](#권한-문제-해결)
6. [Spring Boot 설정](#spring-boot-설정)

## 🏗️ PostgreSQL 구조 이해

### PostgreSQL vs MySQL 구조 비교

#### **PostgreSQL 구조**
```
PostgreSQL Instance
├── Database: postgres (기본)
├── Database: ecommerce (우리가 만든 것)
│   ├── Schema: public (기본 스키마)
│   ├── Schema: member (member_service 소유)
│   ├── Schema: product (product_service 소유)
│   ├── Schema: order (order_service 소유)
│   └── Schema: review (review_service 소유)
└── Database: template1 (템플릿)
```

#### **MySQL 구조**
```
MySQL Instance
├── Database: mysql (시스템)
├── Database: ecommerce (우리가 만든 것)
│   ├── Table: members
│   ├── Table: products
│   ├── Table: orders
│   └── Table: reviews
└── Database: information_schema (시스템)
```

### 핵심 차이점

| 구분 | PostgreSQL | MySQL |
|------|------------|-------|
| **스키마 개념** | ✅ 명시적 스키마 | ❌ 스키마 = 데이터베이스 |
| **네임스페이스** | `schema.table` | `database.table` |
| **권한 관리** | 스키마별 세밀한 권한 | 데이터베이스별 권한 |
| **소유자 개념** | 스키마별 소유자 | 데이터베이스별 권한 |

## 🗄️ 데이터베이스 및 스키마 설정

### Docker Compose 설정

#### **docker-compose.yml**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: ecommerce-postgres
    environment:
      POSTGRES_DB: ecommerce
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-schema.sql:/docker-entrypoint-initdb.d/init-schema.sql
    networks:
      - ecommerce-network

volumes:
  postgres_data:

networks:
  ecommerce-network:
    driver: bridge
```

### 초기화 스크립트

#### **init-schema.sql**
```sql
-- =====================================================
-- PostgreSQL 초기 설정 스크립트
-- =====================================================

-- 1. 사용자 생성
CREATE USER member_service WITH PASSWORD 'member_password' NOSUPERUSER NOCREATEDB NOCREATEROLE;
CREATE USER product_service WITH PASSWORD 'product_password' NOSUPERUSER NOCREATEDB NOCREATEROLE;
CREATE USER order_service WITH PASSWORD 'order_password' NOSUPERUSER NOCREATEDB NOCREATEROLE;
CREATE USER review_service WITH PASSWORD 'review_password' NOSUPERUSER NOCREATEDB NOCREATEROLE;

-- 2. 데이터베이스 접근 권한
GRANT CONNECT ON DATABASE ecommerce TO member_service, product_service, order_service, review_service;

-- 3. 스키마 생성 및 소유권 설정
CREATE SCHEMA member AUTHORIZATION member_service;
CREATE SCHEMA product AUTHORIZATION product_service;
CREATE SCHEMA order AUTHORIZATION order_service;
CREATE SCHEMA review AUTHORIZATION review_service;
CREATE SCHEMA common AUTHORIZATION postgres;

-- 4. 공통 시퀀스 생성
CREATE SEQUENCE common.snowflake_id_seq OWNER TO postgres;

-- 5. 스키마 권한 설정
GRANT USAGE ON SCHEMA common TO member_service, product_service, order_service, review_service;
GRANT USAGE ON SEQUENCE common.snowflake_id_seq TO member_service, product_service, order_service, review_service;

-- 6. 각 스키마별 권한 설정
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA member TO member_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA member TO member_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA member GRANT ALL ON TABLES TO member_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA member GRANT ALL ON SEQUENCES TO member_service;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA product TO product_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA product TO product_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT ALL ON TABLES TO product_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT ALL ON SEQUENCES TO product_service;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA order TO order_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA order TO order_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA order GRANT ALL ON TABLES TO order_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA order GRANT ALL ON SEQUENCES TO order_service;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA review TO review_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA review TO review_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA review GRANT ALL ON TABLES TO review_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA review GRANT ALL ON SEQUENCES TO review_service;
```

## 👤 사용자 생성 및 권한 설정

### 사용자 생성 스크립트

#### **create-users.sh**
```bash
#!/bin/bash

echo "👤 PostgreSQL 사용자 설정 시작..."

# PostgreSQL 접속
docker exec -it ecommerce-postgres psql -U postgres -d ecommerce << EOF

-- 사용자 생성
CREATE USER member_service WITH PASSWORD 'member_password' NOSUPERUSER NOCREATEDB NOCREATEROLE;
CREATE USER product_service WITH PASSWORD 'product_password' NOSUPERUSER NOCREATEDB NOCREATEROLE;
CREATE USER order_service WITH PASSWORD 'order_password' NOSUPERUSER NOCREATEDB NOCREATEROLE;
CREATE USER review_service WITH PASSWORD 'review_password' NOSUPERUSER NOCREATEDB NOCREATEROLE;

-- 데이터베이스 접근 권한
GRANT CONNECT ON DATABASE ecommerce TO member_service, product_service, order_service, review_service;

-- 스키마 생성
CREATE SCHEMA member AUTHORIZATION member_service;
CREATE SCHEMA product AUTHORIZATION product_service;
CREATE SCHEMA order AUTHORIZATION order_service;
CREATE SCHEMA review AUTHORIZATION review_service;

-- 권한 설정
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA member TO member_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA member TO member_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA member GRANT ALL ON TABLES TO member_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA member GRANT ALL ON SEQUENCES TO member_service;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA product TO product_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA product TO product_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT ALL ON TABLES TO product_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT ALL ON SEQUENCES TO product_service;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA order TO order_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA order TO order_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA order GRANT ALL ON TABLES TO order_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA order GRANT ALL ON SEQUENCES TO order_service;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA review TO review_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA review TO review_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA review GRANT ALL ON TABLES TO review_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA review GRANT ALL ON SEQUENCES TO review_service;

\q
EOF

echo "✅ 사용자 설정 완료!"

# 접속 테스트
echo "🧪 접속 테스트 중..."
docker exec -it ecommerce-postgres psql -U member_service -d ecommerce -c "SELECT current_user, current_database();"
docker exec -it ecommerce-postgres psql -U product_service -d ecommerce -c "SELECT current_user, current_database();"
```

### 권한 확인 명령어

#### **권한 확인**
```sql
-- 사용자 목록 확인
\du

-- 스키마 소유자 확인
SELECT nspname, nspowner::regrole FROM pg_namespace WHERE nspname = 'member';

-- 스키마 권한 확인
SELECT 
    nspname as schema_name,
    has_schema_privilege('member_service', nspname, 'CREATE') as can_create,
    has_schema_privilege('member_service', nspname, 'USAGE') as can_use
FROM pg_namespace 
WHERE nspname = 'member';
```

## 🗂️ 테이블 생성

### Member 엔티티 기반 테이블 생성

#### **create-member-tables.sql**
```sql
-- =====================================================
-- Member Service 테이블 생성 스크립트 (Snowflake ID)
-- =====================================================

-- 1. Member 테이블 생성
CREATE TABLE IF NOT EXISTS member.member (
    id BIGINT PRIMARY KEY,  -- Snowflake ID
    email VARCHAR(150) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    birth_date DATE,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('MAIL', 'FEMAIL')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'WITHDRAWN')),
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 제약조건
    CONSTRAINT UK_MEMBER_EMAIL_ADDRESS UNIQUE (email)
);

-- 2. MemberRole 테이블 생성
CREATE TABLE IF NOT EXISTS member.member_role (
    id BIGINT PRIMARY KEY,  -- Snowflake ID
    member_id BIGINT NOT NULL,
    role_type VARCHAR(10) NOT NULL CHECK (role_type IN ('BUYER', 'SELLER', 'ADMIN')),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 외래키 제약조건
    CONSTRAINT FK_MEMBER_ROLE_MEMBER_ID FOREIGN KEY (member_id) 
        REFERENCES member.member(id) ON DELETE CASCADE
);

-- 3. 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_member_email ON member.member(email);
CREATE INDEX IF NOT EXISTS idx_member_status ON member.member(status);
CREATE INDEX IF NOT EXISTS idx_member_create_at ON member.member(create_at);

CREATE INDEX IF NOT EXISTS idx_member_role_member_id ON member.member_role(member_id);
CREATE INDEX IF NOT EXISTS idx_member_role_role_type ON member.member_role(role_type);
CREATE INDEX IF NOT EXISTS idx_member_role_assigned_at ON member.member_role(assigned_at);

-- 4. 권한 설정
GRANT ALL PRIVILEGES ON TABLE member.member TO member_service;
GRANT ALL PRIVILEGES ON TABLE member.member_role TO member_service;
```

### 테이블 생성 스크립트

#### **create-member-tables.sh**
```bash
#!/bin/bash

echo "️ Member Service 테이블 생성 시작 (Snowflake ID)..."

# PostgreSQL 접속하여 테이블 생성
docker exec -it ecommerce-postgres psql -U postgres -d ecommerce << EOF

-- Member 테이블 생성 (Snowflake ID)
CREATE TABLE IF NOT EXISTS member.member (
    id BIGINT PRIMARY KEY,  -- Snowflake ID
    email VARCHAR(150) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    birth_date DATE,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('MAIL', 'FEMAIL')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'WITHDRAWN')),
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT UK_MEMBER_EMAIL_ADDRESS UNIQUE (email)
);

-- MemberRole 테이블 생성 (Snowflake ID)
CREATE TABLE IF NOT EXISTS member.member_role (
    id BIGINT PRIMARY KEY,  -- Snowflake ID
    member_id BIGINT NOT NULL,
    role_type VARCHAR(10) NOT NULL CHECK (role_type IN ('BUYER', 'SELLER', 'ADMIN')),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_MEMBER_ROLE_MEMBER_ID FOREIGN KEY (member_id) 
        REFERENCES member.member(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_member_email ON member.member(email);
CREATE INDEX IF NOT EXISTS idx_member_status ON member.member(status);
CREATE INDEX IF NOT EXISTS idx_member_create_at ON member.member(create_at);

CREATE INDEX IF NOT EXISTS idx_member_role_member_id ON member.member_role(member_id);
CREATE INDEX IF NOT EXISTS idx_member_role_role_type ON member.member_role(role_type);
CREATE INDEX IF NOT EXISTS idx_member_role_assigned_at ON member.member_role(assigned_at);

\q
EOF

echo "✅ Member Service 테이블 생성 완료 (Snowflake ID)!"

# 테이블 확인
echo "📋 생성된 테이블 확인:"
docker exec -it ecommerce-postgres psql -U postgres -d ecommerce -c "\d member.member"
docker exec -it ecommerce-postgres psql -U postgres -d ecommerce -c "\d member.member_role"
```

### 테스트 데이터 삽입

#### **insert-test-data.sql**
```sql
-- 테스트 회원 데이터 삽입 (Snowflake ID 수동 생성)
INSERT INTO member.member (id, email, name, password, birth_date, gender, status, create_at) 
VALUES 
    (1234567890123456789, 'user1@example.com', '홍길동', '$2a$10$encrypted_password', '1990-01-01', 'MAIL', 'ACTIVE', CURRENT_TIMESTAMP),
    (1234567890123456790, 'user2@example.com', '김철수', '$2a$10$encrypted_password', '1995-05-15', 'MAIL', 'ACTIVE', CURRENT_TIMESTAMP),
    (1234567890123456791, 'user3@example.com', '이영희', '$2a$10$encrypted_password', '1988-12-25', 'FEMAIL', 'ACTIVE', CURRENT_TIMESTAMP);

-- 회원 역할 데이터 삽입
INSERT INTO member.member_role (id, member_id, role_type, assigned_at)
VALUES 
    (1234567890123456792, 1234567890123456789, 'BUYER', CURRENT_TIMESTAMP),
    (1234567890123456793, 1234567890123456790, 'BUYER', CURRENT_TIMESTAMP),
    (1234567890123456794, 1234567890123456791, 'BUYER', CURRENT_TIMESTAMP);
```

## 🔧 권한 문제 해결

### 권한 문제 해결 스크립트

#### **fix-permission-issue.sh**
```bash
#!/bin/bash

echo "🔐 PostgreSQL 권한 문제 해결 중..."

# postgres 사용자로 접속하여 권한 재설정
docker exec -it ecommerce-postgres psql -U postgres -d ecommerce << EOF

-- 1. 기존 스키마 삭제 (테이블이 있다면 함께 삭제)
DROP SCHEMA IF EXISTS member CASCADE;
DROP SCHEMA IF EXISTS product CASCADE;
DROP SCHEMA IF EXISTS order CASCADE;
DROP SCHEMA IF EXISTS review CASCADE;

-- 2. 사용자 재생성
DROP USER IF EXISTS member_service;
CREATE USER member_service WITH PASSWORD 'member_password' NOSUPERUSER NOCREATEDB NOCREATEROLE;

-- 3. 데이터베이스 접근 권한
GRANT CONNECT ON DATABASE ecommerce TO member_service;

-- 4. 스키마 생성 (소유자 지정)
CREATE SCHEMA member AUTHORIZATION member_service;
CREATE SCHEMA product AUTHORIZATION product_service;
CREATE SCHEMA order AUTHORIZATION order_service;
CREATE SCHEMA review AUTHORIZATION review_service;
CREATE SCHEMA common AUTHORIZATION postgres;

-- 5. 스키마 사용 권한
GRANT USAGE ON SCHEMA member TO member_service;
GRANT USAGE ON SCHEMA product TO product_service;
GRANT USAGE ON SCHEMA order TO order_service;
GRANT USAGE ON SCHEMA review TO review_service;
GRANT USAGE ON SCHEMA common TO member_service, product_service, order_service, review_service;

-- 6. 테이블 생성 권한
GRANT CREATE ON SCHEMA member TO member_service;
GRANT CREATE ON SCHEMA product TO product_service;
GRANT CREATE ON SCHEMA order TO order_service;
GRANT CREATE ON SCHEMA review TO review_service;

-- 7. 향후 생성될 테이블에 대한 권한
ALTER DEFAULT PRIVILEGES IN SCHEMA member GRANT ALL ON TABLES TO member_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA member GRANT ALL ON SEQUENCES TO member_service;

ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT ALL ON TABLES TO product_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT ALL ON SEQUENCES TO product_service;

ALTER DEFAULT PRIVILEGES IN SCHEMA order GRANT ALL ON TABLES TO order_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA order GRANT ALL ON SEQUENCES TO order_service;

ALTER DEFAULT PRIVILEGES IN SCHEMA review GRANT ALL ON TABLES TO review_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA review GRANT ALL ON SEQUENCES TO review_service;

\q
EOF

echo "✅ 권한 재설정 완료!"

# 권한 테스트
echo "🧪 권한 테스트 중..."
docker exec -it ecommerce-postgres psql -U member_service -d ecommerce -c "SELECT current_user, current_database();"
docker exec -it ecommerce-postgres psql -U member_service -d ecommerce -c "\dn"
```

### 권한 확인 명령어

#### **권한 확인**
```sql
-- 스키마 소유자 확인
SELECT nspname, nspowner::regrole FROM pg_namespace WHERE nspname = 'member';

-- 사용자 권한 확인
SELECT 
    usename,
    usesuper,
    usecreatedb,
    usecreaterole
FROM pg_user 
WHERE usename = 'member_service';

-- 스키마 권한 확인
SELECT 
    nspname as schema_name,
    has_schema_privilege('member_service', nspname, 'CREATE') as can_create,
    has_schema_privilege('member_service', nspname, 'USAGE') as can_use
FROM pg_namespace 
WHERE nspname = 'member';
```

## ⚙️ Spring Boot 설정

### application.yml 설정

#### **application.yml**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce
    username: member_service
    password: member_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: member
        format_sql: true
    show-sql: true
  sql:
    init:
      mode: always
```

### 프로파일별 설정

#### **application-dev.yml**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce
    username: member_service
    password: member_password
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        default_schema: member
```

#### **application-prod.yml**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/ecommerce
    username: member_service
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        default_schema: member
```

## 📊 유용한 조회 명령어

### 데이터베이스 관련

```sql
-- 데이터베이스 목록 조회
\l

-- 현재 데이터베이스 확인
SELECT current_database();

-- 데이터베이스 크기
SELECT pg_size_pretty(pg_database_size('ecommerce'));
```

### 스키마 관련

```sql
-- 스키마 목록 조회
\dn

-- 현재 스키마 확인
SHOW search_path;

-- 스키마별 테이블 개수
SELECT 
    schemaname,
    count(*) as table_count
FROM pg_tables 
WHERE schemaname NOT IN ('information_schema', 'pg_catalog')
GROUP BY schemaname;
```

### 테이블 관련

```sql
-- 테이블 목록 조회
\dt member.*

-- 테이블 구조 조회
\d member.member

-- 테이블 크기 조회
SELECT pg_size_pretty(pg_total_relation_size('member.member'));
```

### 사용자/권한 관련

```sql
-- 사용자 목록 조회
\du

-- 현재 사용자 확인
SELECT current_user;

-- 권한 확인
SELECT 
    schema_name,
    privilege_type
FROM information_schema.role_table_grants 
WHERE grantee = current_user;
```

## 🚀 실행 순서

1. **Docker Compose 실행**
   ```bash
   docker-compose up -d
   ```

2. **사용자 및 권한 설정**
   ```bash
   chmod +x create-users.sh
   ./create-users.sh
   ```

3. **테이블 생성**
   ```bash
   chmod +x create-member-tables.sh
   ./create-member-tables.sh
   ```

4. **Spring Boot 애플리케이션 실행**
   ```bash
   ./gradlew :service:member:bootRun
   ```

## 🔍 문제 해결

### 권한 문제
- `permission denied for schema member`: 권한 설정 스크립트 실행
- `database does not exist`: 데이터베이스 생성 확인
- `user does not exist`: 사용자 생성 확인

### 연결 문제
- `connection refused`: Docker 컨테이너 상태 확인
- `authentication failed`: 사용자명/비밀번호 확인
- `database does not exist`: 데이터베이스 존재 여부 확인

이제 PostgreSQL 설정과 테이블 생성을 위한 완전한 가이드가 준비되었습니다! 