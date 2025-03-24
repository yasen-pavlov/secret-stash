package me.bitnet.secretstash.ratelimiter

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.bitnet.secretstash.util.TokenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.method.HandlerMethod
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.Method
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class RateLimiterInterceptorTest {
    @Mock
    private lateinit var rateLimiterService: RedisRateLimiterService

    @Mock
    private lateinit var tokenService: TokenService

    @Mock
    private lateinit var request: HttpServletRequest

    @Mock
    private lateinit var response: HttpServletResponse

    @Mock
    private lateinit var handlerMethod: HandlerMethod

    @Mock
    private lateinit var nonHandlerMethod: Any

    private lateinit var interceptor: RateLimiterInterceptor

    private val testUserId = UUID.fromString("00000000-0000-0000-0000-000000012345")
    private val testUserIdStr = testUserId.toString()

    @BeforeEach
    fun setUp() {
        interceptor =
            RateLimiterInterceptor(
                rateLimiterService = rateLimiterService,
                tokenService = tokenService,
                defaultLimit = 10,
                defaultWindowSeconds = 1,
            )
    }

    @Test
    fun `should pass through if handler is not HandlerMethod`() {
        // Act
        val result = interceptor.preHandle(request, response, nonHandlerMethod)

        // Assert
        assertThat(result).isTrue()
        verifyNoInteractions(rateLimiterService, tokenService)
    }

    @Test
    fun `should pass through if no RateLimit annotation is present`() {
        // Arrange
        val method = mock(Method::class.java)
        `when`(handlerMethod.method).thenReturn(method)
        `when`(handlerMethod.beanType).thenReturn(Any::class.java)
        `when`(method.getAnnotation(RateLimit::class.java)).thenReturn(null)

        // Act
        val result = interceptor.preHandle(request, response, handlerMethod)

        // Assert
        assertThat(result).isTrue()
        verifyNoInteractions(rateLimiterService, tokenService)
    }

    @Test
    fun `should use default values when annotation values are not positive`() {
        // Arrange
        val method = mock(Method::class.java)
        val methodAnnotation = mock(RateLimit::class.java)

        `when`(handlerMethod.method).thenReturn(method)
        `when`(handlerMethod.beanType).thenReturn(Any::class.java)
        `when`(method.getAnnotation(RateLimit::class.java)).thenReturn(methodAnnotation)
        `when`(methodAnnotation.value).thenReturn(0)
        `when`(methodAnnotation.windowSeconds).thenReturn(0)
        `when`(tokenService.getCurrentUserId()).thenReturn(testUserId)
        `when`(rateLimiterService.isAllowed(testUserIdStr, 10, 1)).thenReturn(true)

        // Act
        val result = interceptor.preHandle(request, response, handlerMethod)

        // Assert
        assertThat(result).isTrue()
        verify(rateLimiterService).isAllowed(testUserIdStr, 10, 1)
    }

    @Test
    fun `should use annotation values when they are positive`() {
        // Arrange
        val method = mock(Method::class.java)
        val methodAnnotation = mock(RateLimit::class.java)

        `when`(handlerMethod.method).thenReturn(method)
        `when`(handlerMethod.beanType).thenReturn(Any::class.java)
        `when`(method.getAnnotation(RateLimit::class.java)).thenReturn(methodAnnotation)
        `when`(methodAnnotation.value).thenReturn(20)
        `when`(methodAnnotation.windowSeconds).thenReturn(5)
        `when`(tokenService.getCurrentUserId()).thenReturn(testUserId)
        `when`(rateLimiterService.isAllowed(testUserIdStr, 20, 5)).thenReturn(true)

        // Act
        val result = interceptor.preHandle(request, response, handlerMethod)

        // Assert
        assertThat(result).isTrue()
        verify(rateLimiterService).isAllowed(testUserIdStr, 20, 5)
    }

    @Test
    fun `should use class annotation when method annotation is not present`() {
        // Arrange
        val method = mock(Method::class.java)
        val beanType = TestControllerWithAnnotation::class.java

        `when`(handlerMethod.method).thenReturn(method)
        `when`(handlerMethod.beanType).thenReturn(beanType)
        `when`(method.getAnnotation(RateLimit::class.java)).thenReturn(null)
        `when`(tokenService.getCurrentUserId()).thenReturn(testUserId)
        `when`(rateLimiterService.isAllowed(testUserIdStr, 15, 3)).thenReturn(true)

        // Act
        val result = interceptor.preHandle(request, response, handlerMethod)

        // Assert
        assertThat(result).isTrue()
        verify(rateLimiterService).isAllowed(testUserIdStr, 15, 3)
    }

    @Test
    fun `should return 429 when rate limit is exceeded`() {
        // Arrange
        val method = mock(Method::class.java)
        val methodAnnotation = mock(RateLimit::class.java)
        val responseWriter = StringWriter()
        val printWriter = PrintWriter(responseWriter)

        `when`(handlerMethod.method).thenReturn(method)
        `when`(handlerMethod.beanType).thenReturn(Any::class.java)
        `when`(method.getAnnotation(RateLimit::class.java)).thenReturn(methodAnnotation)
        `when`(methodAnnotation.value).thenReturn(5)
        `when`(methodAnnotation.windowSeconds).thenReturn(2)
        `when`(tokenService.getCurrentUserId()).thenReturn(testUserId)
        `when`(rateLimiterService.isAllowed(testUserIdStr, 5, 2)).thenReturn(false)
        `when`(response.writer).thenReturn(printWriter)

        // Act
        val result = interceptor.preHandle(request, response, handlerMethod)

        // Assert
        assertThat(result).isFalse()
        verify(response).status = 429
        verify(response).contentType = "application/json"
        assertThat(responseWriter.toString()).contains("""{"error":"Rate limit exceeded","retryAfter":2}""")
    }

    @Test
    fun `should allow request when exception occurs during user id retrieval`() {
        // Arrange
        val method = mock(Method::class.java)
        val methodAnnotation = mock(RateLimit::class.java)

        `when`(handlerMethod.method).thenReturn(method)
        `when`(handlerMethod.beanType).thenReturn(Any::class.java)
        `when`(method.getAnnotation(RateLimit::class.java)).thenReturn(methodAnnotation)
        `when`(methodAnnotation.value).thenReturn(5)
        `when`(methodAnnotation.windowSeconds).thenReturn(2)
        `when`(tokenService.getCurrentUserId()).thenThrow(RuntimeException("User not authenticated"))

        // Act
        val result = interceptor.preHandle(request, response, handlerMethod)

        // Assert
        assertThat(result).isTrue()
        verify(rateLimiterService, never()).isAllowed(anyString(), anyInt(), anyInt())
    }

    // Helper class for testing class annotations
    @RateLimit(15, 3)
    private class TestControllerWithAnnotation
}
