package com.vqn.bizflow.backend.invoice.service

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import com.vqn.bizflow.backend.order.dto.OrderResponse
import com.vqn.bizflow.backend.order.service.OrderService
import org.springframework.stereotype.Service
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class InvoiceService(
    private val orderService: OrderService,
) {
    fun generateReceiptPdf(userId: UUID, orderId: UUID, isAdmin: Boolean = false): ByteArray {
        val order = orderService.getById(userId, orderId, isAdmin)
        return buildPdf(order)
    }

    private fun buildPdf(order: OrderResponse): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A5, 36f, 36f, 54f, 36f)
        PdfWriter.getInstance(document, outputStream)
        document.open()

        addHeader(document, order)
        addCustomerInfo(document, order)
        addItemsTable(document, order)
        addTotals(document, order)
        addFooter(document, order)

        document.close()
        return outputStream.toByteArray()
    }

    private fun addHeader(document: Document, order: OrderResponse) {
        val title = Paragraph("HÓA ĐƠN BÁN HÀNG", Font(Font.HELVETICA, 16f, Font.BOLD))
        title.alignment = Element.ALIGN_CENTER
        document.add(title)

        val subtitle = Paragraph(
            "BizFlow — Hệ thống quản lý bán hàng",
            Font(Font.HELVETICA, 9f, Font.ITALIC, Color.DARK_GRAY),
        )
        subtitle.alignment = Element.ALIGN_CENTER
        subtitle.spacingAfter = 12f
        document.add(subtitle)

        val refPara = Paragraph()
        refPara.add(Phrase("Số hóa đơn: ", Font(Font.HELVETICA, 10f, Font.BOLD)))
        refPara.add(Phrase(order.referenceNumber, Font(Font.HELVETICA, 10f)))
        document.add(refPara)

        val datePara = Paragraph()
        datePara.add(Phrase("Ngày lập: ", Font(Font.HELVETICA, 10f, Font.BOLD)))
        datePara.add(Phrase(formatInstant(order.createdAt), Font(Font.HELVETICA, 10f)))
        datePara.spacingAfter = 10f
        document.add(datePara)
    }

    private fun addCustomerInfo(document: Document, order: OrderResponse) {
        val para = Paragraph()
        para.add(Phrase("Mã khách hàng: ", Font(Font.HELVETICA, 10f, Font.BOLD)))
        para.add(Phrase(order.customerId?.toString() ?: "Khách lẻ", Font(Font.HELVETICA, 10f)))
        para.spacingAfter = 10f
        document.add(para)
    }

    private fun addItemsTable(document: Document, order: OrderResponse) {
        val table = PdfPTable(floatArrayOf(4f, 1.5f, 2f, 2.5f))
        table.widthPercentage = 100f

        addTableHeaderCell(table, "Sản phẩm")
        addTableHeaderCell(table, "SL")
        addTableHeaderCell(table, "Đơn giá")
        addTableHeaderCell(table, "Thành tiền")

        order.items.forEach { item ->
            table.addCell(PdfPCell(Phrase(item.productName, Font(Font.HELVETICA, 9f))))
            table.addCell(
                PdfPCell(Phrase(formatQuantity(item.quantity), Font(Font.HELVETICA, 9f)))
                    .apply { horizontalAlignment = Element.ALIGN_RIGHT },
            )
            table.addCell(
                PdfPCell(Phrase(formatMoney(item.unitPrice), Font(Font.HELVETICA, 9f)))
                    .apply { horizontalAlignment = Element.ALIGN_RIGHT },
            )
            table.addCell(
                PdfPCell(Phrase(formatMoney(item.subtotal), Font(Font.HELVETICA, 9f, Font.BOLD)))
                    .apply { horizontalAlignment = Element.ALIGN_RIGHT },
            )
        }
        document.add(table)
    }

    private fun addTableHeaderCell(table: PdfPTable, text: String) {
        val cell = PdfPCell(Phrase(text, Font(Font.HELVETICA, 9f, Font.BOLD, Color.WHITE)))
        cell.backgroundColor = Color(63, 81, 181)
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.paddingBottom = 6f
        table.addCell(cell)
    }

    private fun addTotals(document: Document, order: OrderResponse) {
        document.add(Paragraph(" ", Font(Font.HELVETICA, 6f)))

        addTotalLine(document, "Tổng tiền hàng:", formatMoney(order.totalAmount), bold = true)
        addTotalLine(document, "Đã thanh toán:", formatMoney(order.paidAmount))
        addTotalLine(document, "Còn nợ:", formatMoney(order.debtAmount), bold = true, color = Color.RED)
    }

    private fun addTotalLine(
        document: Document,
        label: String,
        value: String,
        bold: Boolean = false,
        color: Color = Color.BLACK,
    ) {
        val para = Paragraph()
        para.alignment = Element.ALIGN_RIGHT
        val font = if (bold) {
            Font(Font.HELVETICA, 10f, Font.BOLD, color)
        } else {
            Font(Font.HELVETICA, 10f, Font.NORMAL, color)
        }
        para.add(Phrase("$label ", Font(Font.HELVETICA, 10f)))
        para.add(Phrase(value, font))
        document.add(para)
    }

    private fun addFooter(document: Document, order: OrderResponse) {
        document.add(Paragraph(" ", Font(Font.HELVETICA, 12f)))
        val notePara = Paragraph()
        notePara.add(Phrase("Ghi chú: ", Font(Font.HELVETICA, 9f, Font.BOLD)))
        notePara.add(Phrase(order.notes ?: "—", Font(Font.HELVETICA, 9f, Font.ITALIC)))
        document.add(notePara)

        val thankYou = Paragraph("Cảm ơn quý khách!", Font(Font.HELVETICA, 10f, Font.BOLD))
        thankYou.alignment = Element.ALIGN_CENTER
        thankYou.spacingBefore = 16f
        document.add(thankYou)

        val status = Paragraph(
            "Trạng thái: ${order.status}",
            Font(Font.HELVETICA, 8f, Font.ITALIC, Color.GRAY),
        )
        status.alignment = Element.ALIGN_CENTER
        document.add(status)
    }

    private fun formatMoney(amount: BigDecimal): String {
        val longVal = amount.toLong()
        return "%,d VNĐ".format(longVal)
    }

    private fun formatQuantity(quantity: BigDecimal): String {
        val stripped = quantity.stripTrailingZeros()
        return if (stripped.scale() <= 0) stripped.toLong().toString() else stripped.toPlainString()
    }

    private fun formatInstant(instant: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
        return formatter.format(instant)
    }
}
