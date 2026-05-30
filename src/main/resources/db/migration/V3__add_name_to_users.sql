-- V3__add_name_to_users.sql
-- Adds name column to users table for OIDC profile scope

ALTER TABLE users ADD name NVARCHAR(255) NULL;
