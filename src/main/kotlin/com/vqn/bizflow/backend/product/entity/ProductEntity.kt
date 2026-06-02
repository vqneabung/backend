package com.vqn.bizflow.backend.product.entity

import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Sản phẩm của chủ cửa hàng (Owner).
 *
 * Soft delete: is_active = false thay vì xóa vật lý.
 * Optimistic locking: @Version để tránh race condition khi 2 user cùng sửa.
 * Snapshot giá: giá tại thời điểm bán được lưu riêng ở đơn hàng (không lấy từ đây).
 *
 * Kế thừa BaseEntity: id (UUID) + createdAt.
 * updatedAt khai báo riêng tại đây (không ở BaseEntity vì Hibernate 7 auto-detect).
 */
@Entity
@Table(
    name = "products",
    indexes = [
        Index(name = "idx_products_owner_id", columnList = "owner_id"),
        Index(name = "idx_products_barcode", columnList = "barcode"),
    ]
)
class ProductEntity(
    /** User ID của chủ cửa hàng (UUID) — FK → users(id) */
    @Column(nullable = false)
    val ownerId: UUID,

    /** Tên sản phẩm — unique trong cùng owner (khi is_active = true) */
    @Column(nullable = false, length = 255)
    var name: String,

    /** Danh mục: VLXD, Tạp hóa... */
    @Column(length = 100)
    var category: String? = null,

    /** Đơn vị tính chính: Bao, Kg, Thùng... */
    @Column(nullable = false, length = 50)
    var primaryUnit: String,

    /** Giá bán (VND) — phải > 0 */
    @Column(nullable = false, precision = 18, scale = 0)
    var price: BigDecimal,

    /** Giá vốn (tuỳ chọn) — dùng cho báo cáo lợi nhuận */
    @Column(precision = 18, scale = 0)
    var costPrice: BigDecimal? = null,

    /** Tồn kho hiện tại — không được âm */
    @Column(nullable = false, precision = 18, scale = 2)
    var stock: BigDecimal = BigDecimal.ZERO,

    /** Tồn tối thiểu — cảnh báo khi tồn kho dưới ngưỡng */
    @Column(nullable = false, precision = 18, scale = 2)
    var minStock: BigDecimal = BigDecimal.ZERO,

    /** URL hình ảnh sản phẩm */
    @Column(length = 500)
    var imageUrl: String? = null,

    /** Mã vạch — unique nếu có */
    @Column(length = 100)
    var barcode: String? = null,

    /** Soft delete flag — false = ẩn sản phẩm */
    @Column(nullable = false)
    var isActive: Boolean = true,

    /** Optimistic locking — tự động tăng khi update */
    @Version
    @Column(nullable = false)
    var version: Long = 0,

    /** Thời điểm cập nhật cuối */
    @Column
    var updatedAt: Instant? = null,
) : BaseEntity()
