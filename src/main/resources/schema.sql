-- schema.sql — OAuth2 tables cho Spring Authorization Server.
-- Các bảng này không phải JPA entity, Spring Security dùng JDBC raw.
-- Hibernate ddl-auto=update không thể tạo được → cần SQL init.

IF OBJECT_ID('oauth2_registered_client') IS NULL
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

IF OBJECT_ID('oauth2_authorization') IS NULL
CREATE TABLE oauth2_authorization (
    id NVARCHAR(100) NOT NULL,
    registered_client_id NVARCHAR(100) NOT NULL,
    principal_name NVARCHAR(200) NOT NULL,
    authorization_grant_type NVARCHAR(100) NOT NULL,
    authorized_scopes NVARCHAR(1000) NULL,
    attributes NVARCHAR(MAX) NULL,
    state NVARCHAR(500) NULL,
    authorization_code_value NVARCHAR(MAX) NULL,
    authorization_code_issued_at DATETIME2 NULL,
    authorization_code_expires_at DATETIME2 NULL,
    authorization_code_metadata NVARCHAR(MAX) NULL,
    access_token_value NVARCHAR(MAX) NULL,
    access_token_issued_at DATETIME2 NULL,
    access_token_expires_at DATETIME2 NULL,
    access_token_metadata NVARCHAR(MAX) NULL,
    access_token_type NVARCHAR(100) NULL,
    access_token_scopes NVARCHAR(1000) NULL,
    oidc_id_token_value NVARCHAR(MAX) NULL,
    oidc_id_token_issued_at DATETIME2 NULL,
    oidc_id_token_expires_at DATETIME2 NULL,
    oidc_id_token_metadata NVARCHAR(MAX) NULL,
    refresh_token_value NVARCHAR(MAX) NULL,
    refresh_token_issued_at DATETIME2 NULL,
    refresh_token_expires_at DATETIME2 NULL,
    refresh_token_metadata NVARCHAR(MAX) NULL,
    user_code_value NVARCHAR(MAX) NULL,
    user_code_issued_at DATETIME2 NULL,
    user_code_expires_at DATETIME2 NULL,
    user_code_metadata NVARCHAR(MAX) NULL,
    device_code_value NVARCHAR(MAX) NULL,
    device_code_issued_at DATETIME2 NULL,
    device_code_expires_at DATETIME2 NULL,
    device_code_metadata NVARCHAR(MAX) NULL,
    CONSTRAINT pk_oauth2_authorization PRIMARY KEY (id)
);

IF OBJECT_ID('oauth2_authorization_consent') IS NULL
CREATE TABLE oauth2_authorization_consent (
    registered_client_id NVARCHAR(100) NOT NULL,
    principal_name NVARCHAR(200) NOT NULL,
    authorities NVARCHAR(1000) NOT NULL,
    CONSTRAINT pk_oauth2_authorization_consent PRIMARY KEY (registered_client_id, principal_name)
);
