package me.bitnet.secretstash.util.ratelimiter

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.bitnet.secretstash.util.auth.TokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RateLimiterInterceptor(
    private val rateLimiterService: RedisRateLimiterService,
    private val tokenService: TokenService,
    @Value("\${rate.limit.default-limit:10}") val defaultLimit: Int,
    @Value("\${rate.limit.window-seconds:1}") val defaultWindowSeconds: Int,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler !is HandlerMethod) {
            return true
        }

        val methodAnnotation = handler.method.getAnnotation(RateLimit::class.java)
        val classAnnotation = handler.beanType.getAnnotation(RateLimit::class.java)

        if (methodAnnotation == null && classAnnotation == null) {
            return true
        }

        val annotationLimit = methodAnnotation?.value ?: classAnnotation.value
        val annotationWindow = methodAnnotation?.windowSeconds ?: classAnnotation.windowSeconds

        val limit = if (annotationLimit > 0) annotationLimit else defaultLimit
        val windowSeconds = if (annotationWindow > 0) annotationWindow else defaultWindowSeconds

        try {
            val userId = tokenService.getCurrentUserId().toString()

            if (!rateLimiterService.isAllowed(userId, limit, windowSeconds)) {
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.contentType = "application/json"
                response.writer.write("""{"error":"Rate limit exceeded","retryAfter":$windowSeconds}""")
                return false
            }

            return true
        } catch (e: Exception) {
            // If we can't get the user ID, allow the request
            // The security framework will handle authentication issues
            return true
        }
    }
}
