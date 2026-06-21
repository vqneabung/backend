package com.vqn.bizflow.backend.invoice.controller

import com.vqn.bizflow.backend.invoice.service.InvoiceService
import com.vqn.bizflow.backend.util.SecurityUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/orders")
class InvoiceController(
    private val invoiceService: InvoiceService,
) {
    private fun Authentication.isAdmin(): Boolean =
        authorities.any { it.authority == "ROLE_ADMIN" }

    @GetMapping("/{orderId}/receipt")
    @PreAuthorize("isAuthenticated()")
    fun downloadReceipt(
        @PathVariable orderId: UUID,
        auth: Authentication,
    ): ResponseEntity<ByteArray> {
        val userId = SecurityUtils.getUserId(auth)
        val pdfBytes = invoiceService.generateReceiptPdf(userId, orderId, isAdmin = auth.isAdmin())

        val filename = "receipt-$orderId.pdf"
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.size.toLong())
            .body(pdfBytes)
    }
}
