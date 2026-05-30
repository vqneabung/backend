package com.vqn.bizflow.backend.auth.service

import com.vqn.bizflow.backend.auth.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

// ===== Cầu nối UserEntity → Spring Security UserDetails =====
// Khi user submit form login (/login), Spring Security gọi loadUserByUsername(email)
// để lấy thông tin user từ DB, sau đó DaoAuthenticationProvider so sánh password với BCrypt
@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    // username từ form login (field name="username") chính là email của user
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        // User builder của Spring Security (không phải UserEntity)
        // password đã được hash BCrypt khi register
        // authorities: ROLE_USER hoặc ROLE_ADMIN (dùng cho @PreAuthorize)
        return User.builder()
            .username(user.email)
            .password(user.password)
            .authorities(listOf(SimpleGrantedAuthority("ROLE_${user.role.name}")))
            .build()
    }
}
