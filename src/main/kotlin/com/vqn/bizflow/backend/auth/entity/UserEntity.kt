package com.vqn.bizflow.backend.auth.entity
import com.vqn.bizflow.backend.entity.BaseEntity
import java.time.Instant
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import java.util.UUID

/**
 * User — người dùng của hệ thống.
 *
 * Kế thừa BaseEntity: id (UUID) + createdAt.
 * Không có updatedAt — User không có update flow trong thiết kế hiện tại.
 *
 * Role hiện tại: USER (Owner), EMPLOYEE (nhân viên của Owner), ADMIN.
 */
@Entity
@Table(name = "users")
@SQLRestriction("is_active = 1")
class UserEntity(
    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER,

    // Tên hiển thị của user — dùng cho OIDC "profile" scope
    // Nullable: không bắt buộc khi register (có thể cập nhật sau)
    @Column(nullable = true)
    var name: String? = null,

    // Thời điểm user xác thực email — null = chưa xác thực
    @Column(name = "email_verified_at", nullable = true)
    var emailVerifiedAt: Instant? = null,

    // Soft delete flag — false = user bị vô hiệu hóa
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    // Nếu non-null: user này là EMPLOYEE thuộc về Owner có id này.
    // Nếu null: user là Owner (USER) hoặc Admin (ADMIN) độc lập.
    @Column(name = "owner_id", nullable = true)
    var ownerId: UUID? = null,
) : BaseEntity()
