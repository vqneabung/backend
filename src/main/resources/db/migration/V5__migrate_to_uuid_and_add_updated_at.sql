-- V5__migrate_to_uuid_and_add_updated_at.sql
-- Migrates all primary keys from BIGINT IDENTITY to uniqueidentifier (UUID)
-- Adds updated_at column to users table
--
-- IMPORTANT: This migration drops and recreates product tables.
-- No data loss in dev — users table is preserved with UUID migration.

-- ===== Step 1: Drop FK-dependent tables =====
DROP TABLE IF EXISTS product_units;
DROP TABLE IF EXISTS products;

-- ===== Step 2: Migrate users table id and add updated_at =====
-- Drop auto-generated PK constraint (name unknown at migration time)
DECLARE @sql NVARCHAR(MAX);
SELECT @sql = 'ALTER TABLE users DROP CONSTRAINT ' + name
FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('users') AND type = 'PK';
EXEC sp_executesql @sql;

-- Drop old BIGINT IDENTITY column
ALTER TABLE users DROP COLUMN id;

-- Add new uniqueidentifier column
ALTER TABLE users ADD id uniqueidentifier NOT NULL;

-- Recreate PK
ALTER TABLE users ADD CONSTRAINT pk_users PRIMARY KEY (id);

-- Add updated_at column (Hibernate 7.x maps Instant → datetimeoffset)
ALTER TABLE users ADD updated_at datetimeoffset(7) NULL;

-- Change created_at from datetime2 to datetimeoffset(7)
-- Hibernate 7.x maps java.time.Instant → datetimeoffset (TIMESTAMP_UTC) by default
-- Must drop DEFAULT constraint before altering column type
DECLARE @df_name NVARCHAR(255);
DECLARE @df_sql NVARCHAR(MAX);
SELECT @df_name = name FROM sys.default_constraints WHERE parent_object_id = OBJECT_ID('users') AND parent_column_id = COLUMNPROPERTY(OBJECT_ID('users'), 'created_at', 'ColumnId');
SET @df_sql = 'ALTER TABLE users DROP CONSTRAINT ' + @df_name;
EXEC sp_executesql @df_sql;
ALTER TABLE users ALTER COLUMN created_at datetimeoffset(7) NOT NULL;
ALTER TABLE users ADD CONSTRAINT df_users_created_at DEFAULT GETUTCDATE() FOR created_at;

-- ===== Step 3: Recreate products table with datetimeoffset(7) for Instant fields =====
CREATE TABLE products (
    id            uniqueidentifier NOT NULL,
    owner_id      uniqueidentifier NOT NULL,
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
    created_at    datetimeoffset(7) NOT NULL DEFAULT GETUTCDATE(),
    updated_at    datetimeoffset(7) NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT fk_products_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

SET QUOTED_IDENTIFIER ON;

CREATE NONCLUSTERED INDEX idx_products_owner_id ON products(owner_id);
CREATE NONCLUSTERED INDEX idx_products_name ON products(name);
CREATE UNIQUE NONCLUSTERED INDEX idx_products_owner_name ON products(owner_id, name) WHERE is_active = 1;
CREATE UNIQUE NONCLUSTERED INDEX idx_products_barcode ON products(barcode) WHERE barcode IS NOT NULL AND is_active = 1;

-- ===== Step 4: Recreate product_units table =====
CREATE TABLE product_units (
    id               uniqueidentifier NOT NULL,
    product_id       uniqueidentifier NOT NULL,
    unit             NVARCHAR(50) NOT NULL,
    price            DECIMAL(18,0) NOT NULL,
    conversion_rate  DECIMAL(18,2) NULL,
    CONSTRAINT pk_product_units PRIMARY KEY (id),
    CONSTRAINT fk_product_units_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uq_product_units UNIQUE (product_id, unit)
);
