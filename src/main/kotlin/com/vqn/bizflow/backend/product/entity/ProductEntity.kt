package com.vqn.bizflow.backend.product.entity

import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.Formula
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
 * FK references:
 * - primaryUnitId → units(id): đơn vị tính chính
 * - categoryId → categories(id): danh mục (nullable)
 *
 * Các @Formula (SQL subquery) để lấy name từ bảng reference mà không
 * cần @ManyToOne read-only — tránh trigger extra SELECT / FK constraint.
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

    /** FK → categories(id) — nullable nếu chưa chọn danh mục */
    @Column
    var categoryId: UUID? = null,

    /** FK → units(id) — đơn vị tính chính */
    @Column(nullable = false)
    var primaryUnitId: UUID,

    /** Giá bán (VND) — phải > 0 */
    @Column(nullable = false, precision = 18, scale = 0)
    var price: BigDecimal,

    /** Giá vốn (tuỳ chọn) — dùng cho báo cáo lợi nhuận */
    @Column(precision = 18, scale = 0)
    var costPrice: BigDecimal? = null,

    /** Tồn kho hiện tại — số nguyên (không dùng số thập phân) */
    @Column(nullable = false, precision = 18, scale = 0)
    var stock: BigDecimal = BigDecimal.ZERO,

    /** Tồn tối thiểu — số nguyên, cảnh báo khi tồn kho dưới ngưỡng */
    @Column(nullable = false, precision = 18, scale = 0)
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

    /**
     * Danh sách hình ảnh (tối đa 5 ảnh / sản phẩm).
     *
     * Cascade ALL: thêm/xóa ProductImageEntity thông qua parent.
     * orphanRemoval = true: khi remove khỏi list, entity bị xóa DB.
     * FetchType.LAZY: tránh N+1 khi list products — dùng fetch join khi cần.
     */
    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @OrderBy("position ASC")
    var images: MutableList<ProductImageEntity> = mutableListOf(),
) : BaseEntity() {

    // ===== @Formula fields (name resolution via SQL subquery, avoids FK join) =====

    /** Tên danh mục — computed via @Formula từ categories table */
    @Formula("(SELECT c.name FROM categories c WHERE c.id = category_id)")
    var categoryName: String? = null

    /** Tên đơn vị tính chính — computed via @Formula từ units table */
    @Formula("(SELECT u.name FROM units u WHERE u.id = primary_unit_id)")
    var primaryUnitName: String? = null

    /**
     * Helper: thêm image với position tự động (cuối danh sách).
     * Giữ helper ở entity để service không phải track position thủ công.
     */
    fun addImage(objectKey: String, uploadedBy: UUID) {
        images.add(
            ProductImageEntity(
                productId = id ?: throw IllegalStateException("Product must be persisted first"),
                objectKey = objectKey,
                position = images.size,
                uploadedBy = uploadedBy,
            )
        )
    }

    /**
     * Helper: thay thế toàn bộ danh sách ảnh (giữ tối đa 5).
     * Dùng khi update — orphanRemoval tự động xóa entity cũ.
     */
    fun replaceImages(objectKeys: List<String>, uploadedBy: UUID) {
        images.clear()
        objectKeys.take(MAX_IMAGES).forEach { key ->
            addImage(key, uploadedBy)
        }
    }

    companion object {
        const val MAX_IMAGES = 5
    }
}
