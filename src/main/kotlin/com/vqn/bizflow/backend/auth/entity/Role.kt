package com.vqn.bizflow.backend.auth.entity

/**
 * Role của user trong hệ thống.
 *
 * - USER: Owner (chủ cửa hàng) — quản lý toàn bộ dữ liệu của mình
 * - EMPLOYEE: Nhân viên của một Owner (thông qua UserEntity.ownerId)
 * - ADMIN: Quản trị hệ thống (global)
 */
enum class Role {
    USER,
    EMPLOYEE,
    ADMIN,
}