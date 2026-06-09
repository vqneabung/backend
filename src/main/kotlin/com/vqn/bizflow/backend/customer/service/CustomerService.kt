package com.vqn.bizflow.backend.customer.service

import com.vqn.bizflow.backend.customer.dto.CreateCustomerRequest
import com.vqn.bizflow.backend.customer.dto.CustomerResponse
import com.vqn.bizflow.backend.customer.dto.UpdateCustomerRequest
import com.vqn.bizflow.backend.customer.dto.toEntity
import com.vqn.bizflow.backend.customer.entity.CustomerEntity
import com.vqn.bizflow.backend.customer.mapper.CustomerMapper
import com.vqn.bizflow.backend.customer.repository.CustomerRepository
import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.exception.BadRequestException
import com.vqn.bizflow.backend.exception.DuplicateException
import com.vqn.bizflow.backend.exception.ResourceNotFoundException
import com.vqn.bizflow.backend.order.dto.OrderSummaryResponse
import com.vqn.bizflow.backend.order.repository.OrderItemRepository
import com.vqn.bizflow.backend.order.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service quản lý khách hàng.
 *
 * Multi-tenant qua ownerId: mỗi owner chỉ thấy customer của mình.
 * Soft delete: is_active = false thay vì xóa vật lý.
 * Optimistic locking: @Version tự động throw khi conflict.
 */
@Service
@Transactional
class CustomerService(
    private val customerRepo: CustomerRepository,
    private val customerMapper: CustomerMapper,
    private val orderRepo: OrderRepository,
    private val orderItemRepo: OrderItemRepository,
    private val messageSource: MessageSource,
) {
    private val log = LoggerFactory.getLogger(CustomerService::class.java)

    /** Helper — lấy i18n message từ MessageSource */
    private fun msg(code: String, vararg args: Any): String =
        messageSource.getMessage(code, args, LocaleContextHolder.getLocale())

    companion object {
        private const val DEFAULT_PAGE = 1
        private const val DEFAULT_SIZE = 20
        private const val MAX_SIZE = 100
    }

    /**
     * Tạo khách hàng mới.
     */
    fun create(userId: UUID, request: CreateCustomerRequest): CustomerResponse {
        val trimmedName = request.name.trim()

        // Check trùng tên (cùng owner)
        if (customerRepo.existsByNameAndOwnerId(trimmedName, userId)) {
            throw DuplicateException(msg("customer.duplicate.name"))
        }

        val entity = request.toEntity(userId)
        val saved = customerRepo.save(entity)

        log.info("Created customer '{}' (id={}) for owner={}", saved.name, saved.id, userId)
        return customerMapper.toResponse(saved)
    }

    /**
     * Cập nhật thông tin khách hàng.
     * Tất cả fields optional — chỉ update field có giá trị.
     */
    fun update(userId: UUID, customerId: UUID, request: UpdateCustomerRequest): CustomerResponse {
        val customer = findActiveCustomer(userId, customerId)

        // Body rỗng
        if (request.name == null && request.phone == null && request.email == null &&
            request.address == null && request.notes == null
        ) {
            throw BadRequestException(msg("customer.update.empty"))
        }

        request.name?.trim()?.let { newName ->
            if (newName != customer.name) {
                if (customerRepo.existsByNameAndOwnerId(newName, userId)) {
                    throw DuplicateException(msg("customer.duplicate.name"))
                }
                customer.name = newName
            }
        }
        request.phone?.let { customer.phone = it.trim() }
        request.email?.let { customer.email = it.trim() }
        request.address?.let { customer.address = it.trim() }
        request.notes?.let { customer.notes = it.trim() }

        val saved = customerRepo.save(customer)
        log.info("Updated customer '{}' (id={})", saved.name, saved.id)
        return customerMapper.toResponse(saved)
    }

    /**
     * Danh sách customers (phân trang + tìm kiếm).
     */
    @Transactional(readOnly = true)
    fun list(
        userId: UUID,
        search: String?,
        page: Int?,
        size: Int?,
    ): PaginationResponse<CustomerResponse> {
        val effectivePage = (page ?: DEFAULT_PAGE).coerceAtLeast(1)
        val effectiveSize = (size ?: DEFAULT_SIZE).coerceIn(1, MAX_SIZE)
        val pageable = PageRequest.of(effectivePage - 1, effectiveSize, Sort.by("name").ascending())

        val resultPage: Page<CustomerEntity> = if (search.isNullOrBlank()) {
            customerRepo.findByOwnerId(userId, pageable)
        } else {
            customerRepo.searchByOwnerId(userId, search.trim(), pageable)
        }

        return PaginationResponse.of(
            data = resultPage.content.map(customerMapper::toResponse),
            page = effectivePage,
            size = effectiveSize,
            totalElements = resultPage.totalElements,
        )
    }

    /**
     * Chi tiết 1 customer.
     */
    @Transactional(readOnly = true)
    fun getById(userId: UUID, customerId: UUID): CustomerResponse {
        val customer = findActiveCustomer(userId, customerId)
        return customerMapper.toResponse(customer)
    }

    /**
     * Ẩn customer (soft delete).
     */
    fun deactivate(userId: UUID, customerId: UUID) {
        val customer = findActiveCustomer(userId, customerId)
        customer.isActive = false
        customerRepo.save(customer)
        log.info("Deactivated customer '{}' (id={})", customer.name, customer.id)
    }

    /**
     * Lịch sử mua hàng của 1 khách hàng (phân trang, mới nhất trước).
     * Dùng cho FR-17 (Customer Purchase History).
     */
    @Transactional(readOnly = true)
    fun getOrders(
        userId: UUID,
        customerId: UUID,
        page: Int?,
        size: Int?,
    ): PaginationResponse<OrderSummaryResponse> {
        // Verify customer tồn tại và thuộc owner
        findActiveCustomer(userId, customerId)

        val effectivePage = (page ?: DEFAULT_PAGE).coerceAtLeast(1)
        val effectiveSize = (size ?: DEFAULT_SIZE).coerceIn(1, MAX_SIZE)
        val pageable = PageRequest.of(effectivePage - 1, effectiveSize, Sort.by(Sort.Direction.DESC, "createdAt"))

        val result = orderRepo.findByOwnerIdAndCustomerId(userId, customerId, pageable)

        // Batch count items tránh N+1 query (1 query thay vì N queries)
        val orderIds = result.content.mapNotNull { it.id }
        val itemCountMap: Map<UUID, Int> = if (orderIds.isEmpty()) {
            emptyMap()
        } else {
            orderItemRepo.countByOrderIds(orderIds)
                .associate { row -> row[0] as UUID to (row[1] as Long).toInt() }
        }

        val summaries = result.content.map { order ->
            OrderSummaryResponse(
                id = requireNotNull(order.id) { "Order ID must not be null" },
                customerId = order.customerId,
                referenceNumber = order.referenceNumber,
                totalAmount = order.totalAmount,
                paidAmount = order.paidAmount,
                debtAmount = order.debtAmount,
                status = order.status,
                itemCount = itemCountMap[order.id] ?: 0,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
            )
        }

        return PaginationResponse.of(
            data = summaries,
            page = effectivePage,
            size = effectiveSize,
            totalElements = result.totalElements,
        )
    }

    // ===== Internal helpers =====

    /** Tìm active customer của specific owner (hoặc throw). */
    private fun findActiveCustomer(userId: UUID, customerId: UUID): CustomerEntity {
        val customer = customerRepo.findById(customerId)
            .orElseThrow { ResourceNotFoundException("Customer not found: $customerId") }

        if (customer.ownerId != userId) {
            throw ResourceNotFoundException(msg("customer.not-found"))
        }

        return customer
    }
}
