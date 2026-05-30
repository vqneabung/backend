-- V2__create_oauth2_tables.sql
-- Creates OAuth2 tables for Spring Authorization Server JDBC implementations

CREATE TABLE oauth2_registered_client (
    id NVARCHAR(100) NOT NULL,
    client_id NVARCHAR(100) NOT NULL,
    client_id_issued_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    client_secret NVARCHAR(200) NULL,
    client_secret_expires_at DATETIME2 NULL,
    client_name NVARCHAR(200) NOT NULL,
    client_authentication_methods NVARCHAR(1000) NOT NULL,
    authorization_grant_types NVARCHAR(1000) NOT NULL,
    redirect_uris NVARCHAR(1000) NULL,
    post_logout_redirect_uris NVARCHAR(1000) NULL,
    scopes NVARCHAR(1000) NOT NULL,
    client_settings NVARCHAR(2000) NOT NULL,
    token_settings NVARCHAR(2000) NOT NULL,
    CONSTRAINT pk_oauth2_registered_client PRIMARY KEY (id)
);

CREATE TABLE oauth2_authorization (
    id NVARCHAR(100) NOT NULL,
    registered_client_id NVARCHAR(100) NOT NULL,
    principal_name NVARCHAR(200) NOT NULL,
    authorization_grant_type NVARCHAR(100) NOT NULL,
    authorized_scopes NVARCHAR(1000) NULL,
    attributes VARBINARY(MAX) NULL,
    state NVARCHAR(500) NULL,
    authorization_code_value VARBINARY(MAX) NULL,
    authorization_code_issued_at DATETIME2 NULL,
    authorization_code_expires_at DATETIME2 NULL,
    authorization_code_metadata VARBINARY(MAX) NULL,
    access_token_value VARBINARY(MAX) NULL,
    access_token_issued_at DATETIME2 NULL,
    access_token_expires_at DATETIME2 NULL,
    access_token_metadata VARBINARY(MAX) NULL,
    access_token_type NVARCHAR(100) NULL,
    access_token_scopes NVARCHAR(1000) NULL,
    oidc_id_token_value VARBINARY(MAX) NULL,
    oidc_id_token_issued_at DATETIME2 NULL,
    oidc_id_token_expires_at DATETIME2 NULL,
    oidc_id_token_metadata VARBINARY(MAX) NULL,
    refresh_token_value VARBINARY(MAX) NULL,
    refresh_token_issued_at DATETIME2 NULL,
    refresh_token_expires_at DATETIME2 NULL,
    refresh_token_metadata VARBINARY(MAX) NULL,
    user_code_value VARBINARY(MAX) NULL,
    user_code_issued_at DATETIME2 NULL,
    user_code_expires_at DATETIME2 NULL,
    user_code_metadata VARBINARY(MAX) NULL,
    device_code_value VARBINARY(MAX) NULL,
    device_code_issued_at DATETIME2 NULL,
    device_code_expires_at DATETIME2 NULL,
    device_code_metadata VARBINARY(MAX) NULL,
    CONSTRAINT pk_oauth2_authorization PRIMARY KEY (id)
);

CREATE TABLE oauth2_authorization_consent (
    registered_client_id NVARCHAR(100) NOT NULL,
    principal_name NVARCHAR(200) NOT NULL,
    authorities NVARCHAR(1000) NOT NULL,
    CONSTRAINT pk_oauth2_authorization_consent PRIMARY KEY (registered_client_id, principal_name)
);
