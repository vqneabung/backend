-- V4__create_products_table.sql
-- Tạo bảng quản lý sản phẩm cho chủ cửa hàng (Owner)
-- Hỗ trợ: multi-unit, soft delete, optimistic locking

CREATE TABLE products (
    id            BIGINT IDENTITY PRIMARY KEY,
    owner_id      BIGINT NOT NULL,
    name          NVARCHAR(255) NOT NULL,
    category      NVARCHAR(100) NULL,
    primary_unit  NVARCHAR(50) NOT NULL,
    price         DECIMAL(18,0) NOT NULL,
    cost_price    DECIMAL(18,0) NULL,
    stock         DECIMAL(18,2) NOT NULL DEFAULT 0,
    min_stock     DECIMAL(18,2) NOT NULL DEFAULT 0,
    image_url     NVARCHAR(500) NULL,
    barcode       NVARCHAR(100) NULL,
    is_active     BIT NOT NULL DEFAULT 1,
    version       BIGINT NOT NULL DEFAULT 0,
    created_at    DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at    DATETIME2 NULL,
    CONSTRAINT fk_products_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

SET QUOTED_IDENTIFIER ON;

CREATE INDEX idx_products_owner_id ON products(owner_id);
CREATE INDEX idx_products_name ON products(name);
CREATE UNIQUE INDEX idx_products_owner_name ON products(owner_id, name) WHERE is_active = 1;
CREATE UNIQUE INDEX idx_products_barcode ON products(barcode) WHERE barcode IS NOT NULL AND is_active = 1;

-- Bảng đơn vị tính phụ (1 sản phẩm có thể có nhiều đơn vị: Bao, Kg...)
CREATE TABLE product_units (
    id               BIGINT IDENTITY PRIMARY KEY,
    product_id       BIGINT NOT NULL,
    unit             NVARCHAR(50) NOT NULL,
    price            DECIMAL(18,0) NOT NULL,
    conversion_rate  DECIMAL(18,2) NULL,
    CONSTRAINT fk_product_units_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uq_product_units UNIQUE (product_id, unit)
);
