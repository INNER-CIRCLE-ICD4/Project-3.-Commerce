-- V1_create_order_tables.sql
-- Commerce Order Service Database Schema
-- PostgreSQL DDL for Order Domain Tables (Entity 코드 기반)

-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS commerce;

-- Set search path for this migration
SET search_path TO commerce, public;

-- =====================================================================
-- 1. ORDER TABLE (OrderEntity.java 기반)
-- =====================================================================
CREATE TABLE IF NOT EXISTS "order" (
    id                  BIGINT PRIMARY KEY,                     -- Snowflake ID
    member_id           BIGINT NOT NULL,                        -- 회원 ID
    order_number        VARCHAR(100) NOT NULL,                  -- 주문 번호
    order_date          TIMESTAMP NOT NULL,                     -- 주문 일시

    -- 배송지 정보
    recipient_name      VARCHAR(100) NOT NULL,                  -- 수령인명
    recipient_phone     VARCHAR(30) NOT NULL,                   -- 수령인 전화번호
    address_code        VARCHAR(20) NOT NULL,                   -- 우편번호
    address             VARCHAR(255) NOT NULL,                  -- 주소
    address_detail      VARCHAR(255) NOT NULL,                  -- 상세주소

    -- 금액 정보
    total_amount        DECIMAL(12,2) NOT NULL,                 -- 할인 전 총액
    total_discount      DECIMAL(12,2) NOT NULL,                 -- 총 할인 금액
    pay_amount          DECIMAL(12,2) NOT NULL,                 -- 실제 결제 금액

    -- 상태 및 시간 정보
    status              VARCHAR(20) NOT NULL,                   -- 주문 상태
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP
);

-- =====================================================================
-- 2. ORDER_ITEM TABLE (OrderItemEntity.java 기반)
-- =====================================================================
CREATE TABLE IF NOT EXISTS order_item (
    id                      BIGINT PRIMARY KEY,                 -- Snowflake ID
    order_id                BIGINT NOT NULL,                    -- 주문 ID (FK)

    -- 상품 정보
    product_id              BIGINT NOT NULL,                    -- 상품 ID
    product_name            VARCHAR(255) NOT NULL,              -- 상품명
    product_price           DECIMAL(12,2) NOT NULL,             -- 상품 단가
    product_option_id       BIGINT NOT NULL,                    -- 상품 옵션 ID
    product_option_name     VARCHAR(255) NOT NULL,              -- 상품 옵션명
    product_discount_price  DECIMAL(12,2) NOT NULL,             -- 단위당 할인 금액
    quantity                INTEGER NOT NULL,                   -- 수량
    total_price             DECIMAL(12,2) NOT NULL,             -- 항목별 최종 금액

    -- 상태 및 시간 정보
    status                  VARCHAR(20) NOT NULL,               -- 주문 항목 상태
    created_at              TIMESTAMP NOT NULL,
    updated_at              TIMESTAMP,

    -- 외래키 제약조건
    CONSTRAINT fk_order_item_order_id FOREIGN KEY (order_id) REFERENCES "order" (id) ON DELETE CASCADE
);

-- =====================================================================
-- 3. ORDER_PAYMENT TABLE (PaymentEntity.java 기반)
-- =====================================================================
CREATE TABLE IF NOT EXISTS order_payment (
    id                  BIGINT PRIMARY KEY,                     -- Snowflake ID
    order_id            BIGINT NOT NULL,                        -- 주문 ID (FK)

    -- 결제 정보
    payment_method      VARCHAR(50) NOT NULL,                   -- 결제 방법
    payment_amount      DECIMAL(12,2) NOT NULL,                 -- 결제 금액
    payment_status      VARCHAR(20) NOT NULL,                   -- 결제 상태
    transaction_id      VARCHAR(100),                           -- 결제 거래 ID
    paid_at             TIMESTAMP,                              -- 결제 완료 시간
    created_at          TIMESTAMP NOT NULL,

    -- 외래키 제약조건
    CONSTRAINT fk_order_payment_order_id FOREIGN KEY (order_id) REFERENCES "order" (id) ON DELETE CASCADE
);

-- =====================================================================
-- 4. SHIPPING TABLE (ShippingEntity.java 기반)
-- =====================================================================
CREATE TABLE IF NOT EXISTS shipping (
    id                  BIGINT PRIMARY KEY,                     -- Snowflake ID
    order_item_id       BIGINT NOT NULL,                        -- 주문 항목 ID (FK)

    -- 배송 정보
    courier             VARCHAR(100),                           -- 택배사
    tracking_number     VARCHAR(100),                           -- 운송장 번호
    shipped_at          TIMESTAMP,                              -- 발송 시간
    delivered_at        TIMESTAMP,                              -- 배송 완료 시간
    current_status      VARCHAR(50),                            -- 현재 배송 상태

    -- 시간 정보
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 외래키 제약조건
    CONSTRAINT fk_shipping_order_item_id FOREIGN KEY (order_item_id) REFERENCES order_item (id) ON DELETE CASCADE
);

-- =====================================================================
-- 5. ORDER_REFUND TABLE (RefundEntity.java 기반)
-- =====================================================================
CREATE TABLE IF NOT EXISTS order_refund (
    id                  BIGINT PRIMARY KEY,                     -- Snowflake ID
    order_item_id       BIGINT NOT NULL,                        -- 주문 항목 ID (FK)

    -- 환불 정보
    reason              TEXT,                                   -- 환불 사유
    refund_amount       DECIMAL(19,2) NOT NULL,                 -- 환불 금액
    refund_status       VARCHAR(20) NOT NULL,                   -- 환불 상태
    requested_at        TIMESTAMP NOT NULL,                     -- 환불 요청 시간
    refunded_at         TIMESTAMP,                              -- 환불 완료 시간

    -- 외래키 제약조건
    CONSTRAINT fk_order_refund_order_item_id FOREIGN KEY (order_item_id) REFERENCES order_item (id) ON DELETE CASCADE
);

-- =====================================================================
-- 6. ORDER_STATUS_HISTORY TABLE (OrderStatusHistoryEntity.java 기반)
-- =====================================================================
CREATE TABLE IF NOT EXISTS order_status_history (
    id                  BIGINT PRIMARY KEY,                     -- Snowflake ID
    order_item_id       BIGINT NOT NULL,                        -- 주문 항목 ID (FK)

    -- 상태 이력 정보
    status              VARCHAR(50) NOT NULL,                   -- 변경된 상태
    changed_at          TIMESTAMP NOT NULL,                     -- 상태 변경 시간
    note                TEXT,                                   -- 상태 변경 메모

    -- 외래키 제약조건
    CONSTRAINT fk_order_status_history_order_item_id FOREIGN KEY (order_item_id) REFERENCES order_item (id) ON DELETE CASCADE
);

-- =====================================================================
-- 7. SHIPPING_STATUS_HISTORY TABLE (ShippingStatusHistoryEntity.java 기반)
-- =====================================================================
CREATE TABLE IF NOT EXISTS shipping_status_history (
    id                  BIGINT PRIMARY KEY,                     -- Snowflake ID
    shipping_id         BIGINT NOT NULL,                        -- 배송 ID (FK)

    -- 배송 상태 이력 정보
    status              VARCHAR(50) NOT NULL,                   -- 배송 상태
    note                TEXT,                                   -- 배송 상태 메모
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 외래키 제약조건
    CONSTRAINT fk_shipping_status_history_shipping_id FOREIGN KEY (shipping_id) REFERENCES shipping (id) ON DELETE CASCADE
);

-- =====================================================================
-- 8. INDEXES (성능 최적화)
-- =====================================================================

-- Order 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_order_member_id ON "order" (member_id);
CREATE INDEX IF NOT EXISTS idx_order_order_number ON "order" (order_number);
CREATE INDEX IF NOT EXISTS idx_order_status ON "order" (status);

-- Order Item 테이블 인덱스 (Entity @Index 어노테이션 기반)
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_item (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_item (product_id);

-- Order Status History 테이블 인덱스 (Entity @Index 어노테이션 기반)
CREATE INDEX IF NOT EXISTS idx_order_status_history_order_item_id ON order_status_history (order_item_id);

-- 기타 성능 인덱스
CREATE INDEX IF NOT EXISTS idx_order_payment_order_id ON order_payment (order_id);
CREATE INDEX IF NOT EXISTS idx_shipping_order_item_id ON shipping (order_item_id);
CREATE INDEX IF NOT EXISTS idx_order_refund_order_item_id ON order_refund (order_item_id);
CREATE INDEX IF NOT EXISTS idx_shipping_status_history_shipping_id ON shipping_status_history (shipping_id);