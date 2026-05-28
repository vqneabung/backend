-- V1__create_users_table.sql
-- Creates the users table matching UserEntity JPA entity

CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(255) NOT NULL,
    password NVARCHAR(255) NOT NULL,
    role NVARCHAR(255) NOT NULL DEFAULT 'USER',
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT uq_users_email UNIQUE (email)
);
