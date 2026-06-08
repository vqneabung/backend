package com.vqn.bizflow.backend.report.service

import com.vqn.bizflow.backend.order.repository.OrderItemRepository
import com.vqn.bizflow.backend.order.repository.OrderRepository
import com.vqn.bizflow.backend.customer.repository.CustomerRepository
import com.vqn.bizflow.backend.product.repository.ProductRepository
import com.vqn.bizflow.backend.report.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import kotlin.streams.toList

/**
 * Service báo cáo thống kê (FR-19 → FR-22).
 *
 * All methods are read-only — no writes.
 * Aggregation được thực hiện qua @Query trong repositories,
 * hoặc Kotlin-side grouping cho daily revenue chart.
 */
@Service
@Transactional(readOnly = true)
class ReportService(
    private val productRepo: ProductRepository,
    private val orderRepo: OrderRepository,
    private val orderItemRepo: OrderItemRepository,
    private val customerRepo: CustomerRepository,
) {
    companion object {
        private const val VIETNAM_TZ = "Asia/Ho_Chi_Minh"
        private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    /**
     * Dashboard overview — stat cards.
     */
    fun getOverview(ownerId: UUID): ReportOverviewResponse {
        val now = LocalDate.now(ZoneId.of(VIETNAM_TZ))
        val startOfMonth = now.withDayOfMonth(1).atStartOfDay(ZoneId.of(VIETNAM_TZ)).toInstant()
        val startOfNextMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.of(VIETNAM_TZ)).toInstant()

        val totalProducts = productRepo.countByOwnerId(ownerId)
        val totalOrdersThisMonth = orderRepo.countByOwnerIdAndStatusAndCreatedAtBetween(
            ownerId, "CONFIRMED", startOfMonth, startOfNextMonth,
        )
        val totalRevenueThisMonth = orderRepo.sumTotalAmountByOwnerIdAndStatusAndCreatedAtBetween(
            ownerId, "CONFIRMED", startOfMonth, startOfNextMonth,
        )
        val totalCustomers = customerRepo.countByOwnerId(ownerId)
        val lowStockCount = productRepo.countLowStock(ownerId)

        return ReportOverviewResponse(
            totalProducts = totalProducts,
            totalOrdersThisMonth = totalOrdersThisMonth,
            totalRevenueThisMonth = totalRevenueThisMonth,
            totalCustomers = totalCustomers,
            lowStockCount = lowStockCount,
        )
    }

    /**
     * Daily revenue for chart.
     * range: 7d | 30d | thisMonth
     */
    fun getRevenue(ownerId: UUID, range: String): RevenueReportResponse {
        val now = LocalDate.now(ZoneId.of(VIETNAM_TZ))
        val tz = ZoneId.of(VIETNAM_TZ)

        val periodStart = when (range) {
            "7d" -> now.minusDays(6)
            "30d" -> now.minusDays(29)
            "thisMonth" -> now.withDayOfMonth(1)
            else -> now.withDayOfMonth(1)
        }
        val periodEnd = now

        val fromInstant = periodStart.atStartOfDay(tz).toInstant()
        val toInstant = periodEnd.plusDays(1).atStartOfDay(tz).toInstant()

        // Fetch all CONFIRMED orders in period
        val orders = orderRepo.findByOwnerIdAndStatusAndCreatedAtBetween(
            ownerId, "CONFIRMED", fromInstant, toInstant,
        )

        // Group by date (Vietnam TZ)
        val dailyRevenue: Map<LocalDate, BigDecimal> = orders.groupBy(
            keySelector = { it.createdAt?.atZone(tz)?.toLocalDate() ?: periodStart },
            valueTransform = { it.totalAmount },
        ).mapValues { (_, amounts) -> amounts.reduce(BigDecimal::add) }

        // Build point list with zero-fill for missing dates
        val points = generateSequence(periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(periodEnd) }
            .map { date ->
                RevenueDailyPoint(
                    date = date.format(DATE_FMT),
                    revenue = dailyRevenue[date] ?: BigDecimal.ZERO,
                )
            }.toList()

        val total = points.map { it.revenue }.reduceOrNull(BigDecimal::add) ?: BigDecimal.ZERO

        return RevenueReportResponse(
            points = points,
            total = total,
            periodStart = periodStart.format(DATE_FMT),
            periodEnd = periodEnd.format(DATE_FMT),
        )
    }

    /**
     * Top N best-selling products.
     */
    fun getBestSelling(ownerId: UUID, limit: Int = 10): BestSellingReportResponse {
        val rows = orderItemRepo.findTopSellingByOwnerId(ownerId)

        val products = rows.take(limit).map { row ->
            BestSellingProduct(
                productId = row[0] as UUID,
                productName = row[1] as String,
                quantitySold = row[2] as BigDecimal,
                revenue = row[3] as BigDecimal,
            )
        }

        return BestSellingReportResponse(products = products)
    }

    /**
     * Inventory status — totals, low stock, category breakdown.
     */
    fun getInventory(ownerId: UUID): InventoryReportResponse {
        val totalProducts = productRepo.countByOwnerId(ownerId)
        val totalValue = productRepo.sumInventoryValue(ownerId)

        // Low stock
        val lowStockEntities = productRepo.findByOwnerIdAndStockLessThanEqualOrderByStockAsc(
            ownerId, BigDecimal.valueOf(Long.MAX_VALUE),
        )
        val lowStockProducts = lowStockEntities
            .filter { it.stock <= it.minStock }
            .map { entity ->
                LowStockProduct(
                    productId = entity.id!!,
                    productName = entity.name,
                    stock = entity.stock,
                    minStock = entity.minStock,
                )
            }

        // Category breakdown
        val categoryRows = productRepo.countByCategory(ownerId)
        val byCategory = categoryRows.map { row ->
            CategoryCount(
                categoryName = row[0] as? String,
                count = row[1] as Long,
            )
        }

        return InventoryReportResponse(
            totalProducts = totalProducts,
            totalValue = totalValue,
            lowStockProducts = lowStockProducts,
            byCategory = byCategory,
        )
    }

    /**
     * Outstanding customer debt report.
     */
    fun getDebt(ownerId: UUID): DebtReportResponse {
        // Query orders with debt from DB
        val ordersWithDebt = orderRepo.findByOwnerIdAndStatusAndCreatedAtBetween(
            ownerId, "CONFIRMED",
            Instant.parse("2000-01-01T00:00:00Z"), // far past
            Instant.now().plusSeconds(86400), // far future
        ).filter { it.debtAmount > BigDecimal.ZERO }

        // Group by customerId
        val debtByCustomer: Map<UUID?, List<Pair<UUID, BigDecimal>>> = ordersWithDebt.groupBy(
            keySelector = { it.customerId },
            valueTransform = { Pair(it.id!!, it.debtAmount) },
        ).mapValues { (_, orders) -> orders }

        // Fetch customer names
        val customerIds = debtByCustomer.keys.filterNotNull()
        val customerNames: Map<UUID, String> = if (customerIds.isEmpty()) {
            emptyMap()
        } else {
            customerRepo.findAllById(customerIds).associate { it.id!! to it.name }
        }

        val customers = debtByCustomer.entries.map { (cId, orders) ->
            val totalDebt = orders.map { it.second }.reduce(BigDecimal::add)
            CustomerDebt(
                customerId = cId,
                customerName = if (cId != null) customerNames[cId] ?: "(deleted)" else "(Walk-in)",
                totalDebt = totalDebt,
                orderCount = orders.size.toLong(),
            )
        }.sortedByDescending { it.totalDebt }

        val totalDebt = customers.map { it.totalDebt }.reduceOrNull(BigDecimal::add) ?: BigDecimal.ZERO

        return DebtReportResponse(
            totalDebt = totalDebt,
            customers = customers,
        )
    }
}
