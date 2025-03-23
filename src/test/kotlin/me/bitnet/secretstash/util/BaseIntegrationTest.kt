package me.bitnet.secretstash.util

import com.fasterxml.jackson.databind.ObjectMapper
import me.bitnet.secretstash.note.infrastructure.JpaNoteRepository
import me.bitnet.secretstash.ratelimiter.RedisRateLimiterService
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import java.util.UUID

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@AutoConfigureMockMvc
abstract class BaseIntegrationTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var redisRateLimiterService: RedisRateLimiterService

    @Autowired
    protected lateinit var jpaNoteRepository: JpaNoteRepository

    // User ID matching the one in WithMockJwt
    protected val testUserId: UUID = UUID.fromString("0c47a356-edb2-47ae-923c-9f2902c622be")

    @BeforeEach
    fun setup() {
        // Reset any existing rate limits for the test user
        redisRateLimiterService.resetLimit(testUserId.toString())

        // Cleanup notes
        jpaNoteRepository.deleteAll()
    }
}
