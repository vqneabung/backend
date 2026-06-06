package com.vqn.bizflow.backend.product.entity

import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.*
import java.util.UUID

/**
 * Hình ảnh sản phẩm — quan hệ 1-n với ProductEntity.
 *
 * Cho phép 1 sản phẩm có tối đa 5 ảnh. Mỗi ảnh lưu MinIO objectKey
 * (VD: "products/uuid.jpg") — frontend dùng `getImageUrl(key)` để resolve
 * presigned URL khi hiển thị.
 *
 * Lifecycle:
 * - Cascade ALL từ ProductEntity.images: thêm/xóa ProductImageEntity
 *   thông qua parent (orphanRemoval = true)
 * - Lazy load mặc định — tránh N+1 khi list products
 *
 * Position field: sắp xếp thứ tự hiển thị (0 = ảnh đầu tiên).
 * Khi chưa có yêu cầu reorder, position = order inserted.
 *
 * Kế thừa BaseEntity để lấy id (UUID) + createdAt.
 */
@Entity
@Table(
    name = "product_images",
    indexes = [
        Index(name = "idx_product_images_product_id", columnList = "product_id"),
        Index(name = "idx_product_images_position", columnList = "product_id,position"),
    ],
)
class ProductImageEntity(
    /** FK → products.id */
    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    /** MinIO object key — VD: "products/uuid.jpg" */
    @Column(nullable = false, length = 500)
    var objectKey: String,

    /** Thứ tự hiển thị (0-4) */
    @Column(nullable = false)
    var position: Int,

    /** User ID đã upload ảnh này */
    @Column(name = "uploaded_by", nullable = false)
    val uploadedBy: UUID,
) : BaseEntity()
