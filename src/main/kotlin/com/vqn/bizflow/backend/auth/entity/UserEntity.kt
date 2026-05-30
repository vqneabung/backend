package com.vqn.bizflow.backend.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

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

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)