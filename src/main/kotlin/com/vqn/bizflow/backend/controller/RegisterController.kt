package com.vqn.bizflow.backend.controller

import com.vqn.bizflow.backend.auth.entity.Role
import com.vqn.bizflow.backend.auth.entity.UserEntity
import com.vqn.bizflow.backend.auth.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.regex.Pattern

/**
 * Controller xử lý trang đăng ký web (Thymeleaf form).
 *
 * Tách biệt khỏi AuthService.register() vì:
 * - API flow (/api/auth/register) → trả JWT token
 * - Web flow (/register) → session-based auth, chỉ cần tạo user → redirect /login
 */
@Controller
class RegisterController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    companion object {
        // Email format validation — dùng chung cho cả client-side và backend
        private val EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    /** Hiển thị trang đăng ký. */
    @GetMapping("/register")
    fun registerPage(): String = "register"

    /**
     * Xử lý form đăng ký (POST /register).
     *
     * @return redirect đến /login?registered nếu thành công, hoặc /register?error nếu thất bại
     */
    @PostMapping("/register")
    fun registerSubmit(
        @RequestParam email: String,
        @RequestParam name: String?,
        @RequestParam password: String,
        @RequestParam confirmPassword: String
    ): String {
        validateRegistration(email, password, confirmPassword)?.let { errorRedirect ->
            return errorRedirect
        }

        if (userRepository.existsByEmail(email.trim())) {
            return "redirect:/register?error=duplicate"
        }

        val hashedPassword = passwordEncoder.encode(password.trim())
            ?: return "redirect:/register?error=invalid"

        val user = UserEntity(
            email = email.trim(),
            password = hashedPassword,
            role = Role.USER,
            name = name?.trim()?.takeIf { it.isNotBlank() }
        )

        userRepository.save(user)
        return "redirect:/login?registered"
    }

    /**
     * Validate input từ form đăng ký.
     * Trả về redirect URL nếu có lỗi, null nếu hợp lệ.
     */
    private fun validateRegistration(
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        if (email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            return "redirect:/register?error=invalid"
        }
        if (password.length < 6) {
            return "redirect:/register?error=invalid"
        }
        if (password != confirmPassword) {
            return "redirect:/register?error=invalid"
        }
        return null
    }
}
