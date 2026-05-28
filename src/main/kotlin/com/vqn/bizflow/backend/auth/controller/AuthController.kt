package com.vqn.bizflow.backend.auth.controller

import com.vqn.bizflow.backend.auth.dto.AuthResponse
import com.vqn.bizflow.backend.auth.dto.LoginRequest
import com.vqn.bizflow.backend.auth.dto.RegisterRequest
import com.vqn.bizflow.backend.auth.service.AuthService
import com.vqn.bizflow.backend.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Authentication", description = "Đăng ký và đăng nhập")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @Operation(
        summary = "Đăng ký tài khoản mới",
        description = "Tạo tài khoản mới và trả về JWT token",
        security = []
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Đăng ký thành công"),
        SwaggerApiResponse(responseCode = "409", description = "Email đã tồn tại"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    ])
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.register(request)
        return ResponseEntity.ok(ApiResponse.success(data = response, message = "Đăng ký thành công"))
    }

    @Operation(
        summary = "Đăng nhập",
        description = "Xác thực email và mật khẩu, trả về JWT token",
        security = []
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
        SwaggerApiResponse(responseCode = "401", description = "Sai email hoặc mật khẩu")
    ])
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.login(request)
        return ResponseEntity.ok(ApiResponse.success(data = response, message = "Đăng nhập thành công"))
    }
}