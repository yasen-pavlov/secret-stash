package me.bitnet.secretstash.config

import me.bitnet.secretstash.ratelimiter.RateLimiterInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig {
    @Bean
    fun webMvcConfigurer(rateLimiterInterceptor: RateLimiterInterceptor): WebMvcConfigurer =
        object : WebMvcConfigurer {
            override fun addInterceptors(registry: InterceptorRegistry) {
                registry.addInterceptor(rateLimiterInterceptor)
            }
        }
}
