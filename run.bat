@echo off
title Bizflow Backend
setlocal disabledelayedexpansion
cd /d "%~dp0"

echo ========================================
echo  Bizflow Backend - Spring Boot Launcher
echo ========================================
echo.

REM ── Step 1: Load .env ──────────────────────────────────────
if exist .env (
    echo [1/3] Loading environment from .env
    for /f "usebackq tokens=1,* delims==" %%i in (
        `findstr /v "^$" .env ^| findstr /v "^#"`
    ) do (
        set "%%i=%%j"
        echo     + %%i
    )
    echo.
) else (
    echo [1/3] No .env file found -- using application defaults
    echo.
)

REM ── Step 2: Apply fallback defaults ────────────────────────
echo [2/3] Verifying required variables...
echo.

if not defined DB_URL (
    set "DB_URL=jdbc:sqlserver://localhost:5144;databaseName=bizflow;encrypt=false;trustServerCertificate=true"
    echo     + DB_URL set to default
)
if not defined DB_USERNAME (
    set "DB_USERNAME=sa"
    echo     + DB_USERNAME set to default
)
if not defined DB_PASSWORD (
    set "DB_PASSWORD=1234567890"
    echo     [WARN] DB_PASSWORD unset -- using dev default. Set in .env!
)
if not defined JPA_DDL_AUTO (
    set "JPA_DDL_AUTO=update"
    echo     + JPA_DDL_AUTO set to default
)
if not defined JPA_SHOW_SQL (
    set "JPA_SHOW_SQL=true"
    echo     + JPA_SHOW_SQL set to default
)
if not defined JWT_EXPIRATION (
    set "JWT_EXPIRATION=86400000"
    echo     + JWT_EXPIRATION set to default
)
if not defined AUTH_ISSUER (
    set "AUTH_ISSUER=http://localhost:8080"
    echo     + AUTH_ISSUER set to default
)
if not defined NEXTJS_CLIENT_SECRET (
    set "NEXTJS_CLIENT_SECRET=nextjs-secret"
    echo     + NEXTJS_CLIENT_SECRET set to default
)
if not defined NEXTJS_REDIRECT_URI (
    set "NEXTJS_REDIRECT_URI=http://localhost:3000/api/auth/callback/oidc"
    echo     + NEXTJS_REDIRECT_URI set to default
)
if not defined NEXTJS_POST_LOGOUT_URI (
    set "NEXTJS_POST_LOGOUT_URI=http://localhost:3000/"
    echo     + NEXTJS_POST_LOGOUT_URI set to default
)
if not defined LARAVEL_CLIENT_SECRET (
    set "LARAVEL_CLIENT_SECRET=admin-secret"
    echo     + LARAVEL_CLIENT_SECRET set to default
)
if not defined LARAVEL_REDIRECT_URI (
    set "LARAVEL_REDIRECT_URI=http://localhost:8000/admin/callback"
    echo     + LARAVEL_REDIRECT_URI set to default
)
if not defined FRONTEND_URL (
    set "FRONTEND_URL=http://localhost:3000"
    echo     + FRONTEND_URL set to default
)
if not defined ADMIN_URL (
    set "ADMIN_URL=http://localhost:8000"
    echo     + ADMIN_URL set to default
)

echo.

REM ── Step 3: Run Gradle ─────────────────────────────────────
echo [3/3] Starting Bizflow Backend...
echo.

if "%~1"=="" (
    echo Default task: bootRun
    echo To run other tasks: run.bat build ^| test ^| clean ^| tasks
    echo.
    call gradlew.bat bootRun
) else (
    echo Task: %*
    echo.
    call gradlew.bat %*
)

endlocal
