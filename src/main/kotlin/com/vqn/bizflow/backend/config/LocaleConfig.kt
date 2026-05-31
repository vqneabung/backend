package com.vqn.bizflow.backend.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import java.time.Duration
import java.util.Locale

/**
 * LocaleConfig — Cấu hình i18n cho Spring Boot.
 *
 * Cơ chế:
 * - MessageSource: đọc messages.properties + messages_vi.properties
 * - AcceptHeaderLocaleResolver: detect locale từ Accept-Language header
 * - LocaleChangeInterceptor: đổi locale qua ?lang=vi hoặc ?lang=en
 *
 * File messages:
 * - src/main/resources/messages.properties → English (mặc định)
 * - src/main/resources/messages_vi.properties → Tiếng Việt
 */
@Configuration
class LocaleConfig : WebMvcConfigurer {

    /**
     * MessageSource — Load messages từ file properties.
     * - basename: classpath:messages (tìm messages_*.properties)
     * - defaultEncoding: UTF-8 (hỗ trợ tiếng Việt)
     * - fallbackToSystemLocale: false → fallback về messages.properties
     */
    @Bean
    fun messageSource(): MessageSource {
        return ReloadableResourceBundleMessageSource().apply {
            setBasename("classpath:messages")
            setDefaultEncoding("UTF-8")
            setFallbackToSystemLocale(false)
        }
    }

    /**
     * LocaleResolver — Detect locale từ Accept-Language header.
     * - Default locale: Vietnamese (vi) — khớp với mục tiêu người dùng chính
     * - Fallback: English (nếu header không gửi locale phù hợp)
     * - Không cần cookie: Next.js gửi locale qua Accept-Language header
     */
    @Bean
    fun localeResolver(): AcceptHeaderLocaleResolver {
        return AcceptHeaderLocaleResolver().apply {
            setDefaultLocale(Locale.forLanguageTag("vi-VN"))
        }
    }

    /**
     * LocaleChangeInterceptor — Cho phép đổi locale qua request param.
     * URL: /api/auth/login?lang=en → chuyển sang tiếng Anh
     *
     * Lưu ý: Interceptor này yêu cầu LocaleResolver hỗ trợ setLocale().
     * AcceptHeaderLocaleResolver KHÔNG hỗ trợ setLocale → interceptor này
     * chỉ hoạt động nếu dùng SessionLocaleResolver hoặc CookieLocaleResolver.
     * Để đơn giản, bỏ qua interceptor — locale từ header là đủ.
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        // AcceptHeaderLocaleResolver không hỗ trợ setLocale → bỏ qua interceptor
    }
}
