package com.vqn.bizflow.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

/**
 * Base entity chứa các trường chung cho tất cả entities.
 *
 * - id: UUID (Hibernate @UuidGenerator, kiểu uniqueidentifier trong SQL Server)
 * - createdAt: thời điểm tạo, không thể sửa sau khi persist
 * - updatedAt: thời điểm cập nhật cuối (nullable)
 *
 * Entities kế thừa BaseEntity sẽ tự động có 3 trường này,
 * không cần khai báo lại trong constructor.
 *
 * @see ProductEntity
 * @see UserEntity
 */
@MappedSuperclass
abstract class BaseEntity(
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    open val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, updatable = false)
    open val createdAt: Instant = Instant.now(),

    @Column
    open var updatedAt: Instant? = null,
)
