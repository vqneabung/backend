package com.vqn.bizflow.backend.auth.entity
import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

/**
 * User — người dùng của hệ thống.
 *
 * Kế thừa BaseEntity: id (UUID) + createdAt.
 * Không có updatedAt — User không có update flow trong thiết kế hiện tại.
 *
 * Role hiện tại: USER, ADMIN.
 */
@Entity
@Table(name = "users")
class UserEntity(
    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.USER,

    // Tên hiển thị của user — dùng cho OIDC "profile" scope
    // Nullable: không bắt buộc khi register (có thể cập nhật sau)
    @Column(nullable = true)
    val name: String? = null,
) : BaseEntity()
