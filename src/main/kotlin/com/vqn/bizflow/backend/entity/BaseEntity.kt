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
 *
 * id nullable với default null — khi entity được tạo mới, save() gọi persist().
 * Sau khi persist, @UuidGenerator sinh UUID và Hibernate set vào entity.
 *
 * KHÔNG đặt updatedAt ở đây — Hibernate 7.x auto-detect
 * timestamp fields as optimistic lock, gây StaleObjectStateException.
 */
@MappedSuperclass
abstract class BaseEntity(
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    open var id: UUID? = null,

    @Column(nullable = false, updatable = false)
    open val createdAt: Instant = Instant.now(),
)
