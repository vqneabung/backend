package com.vqn.bizflow.backend.product.entity

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.util.UUID

/**
 * Đơn vị tính phụ của sản phẩm.
 *
 * Cho phép 1 sản phẩm có nhiều đơn vị tính khác nhau,
 * mỗi đơn vị có giá riêng. Ví dụ:
 * - Xi măng: giá theo Bao (85,000₫) và theo Kg (1,700₫)
 * - conversionRate: 1 Bao = 50 Kg
 *
 * Không kế thừa BaseEntity (không cần createdAt/updatedAt).
 * id là UUID riêng, dùng @UuidGenerator.
 */
@Entity
@Table(name = "product_units")
class ProductUnitEntity(
    @Id
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val productId: UUID,

    @Column(nullable = false, length = 50)
    var unit: String,

    @Column(nullable = false, precision = 18, scale = 0)
    var price: BigDecimal,

    @Column(precision = 18, scale = 2)
    var conversionRate: BigDecimal? = null,
)
