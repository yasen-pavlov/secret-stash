package me.bitnet.secretstash.ratelimiter

import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.util.BaseIntegrationTest
import me.bitnet.secretstash.util.WithMockJwt
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@TestPropertySource(
    properties = [
        "rate.limit.default-limit=3",
        "rate.limit.window-seconds=5",
    ],
)
class RateLimiterIntegrationTest : BaseIntegrationTest() {
    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should allow requests within rate limit`() {
        val noteRequest = NoteRequest("Test Title", "Test Content")
        val requestBody = objectMapper.writeValueAsString(noteRequest)

        // Make requests up to the limit (3 requests in 5 seconds)
        for (i in 1..3) {
            mockMvc
                .perform(
                    post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(status().isOk())
        }
    }

    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should block requests exceeding rate limit`() {
        val noteRequest = NoteRequest("Test Title", "Test Content")
        val requestBody = objectMapper.writeValueAsString(noteRequest)

        // Make requests up to the limit
        for (i in 1..3) {
            mockMvc
                .perform(
                    post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(status().isOk())
        }

        // This request should exceed the rate limit
        mockMvc
            .perform(
                post("/api/notes")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andExpect(status().isTooManyRequests())
    }

    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should reset rate limit after window expires`() {
        val noteRequest = NoteRequest("Test Title", "Test Content")
        val requestBody = objectMapper.writeValueAsString(noteRequest)

        // Make requests up to the limit
        for (i in 1..3) {
            mockMvc
                .perform(
                    post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(status().isOk())
        }

        // Simulate waiting for rate limit window to expire by resetting the limit
        redisRateLimiterService.resetLimit(testUserId.toString())

        // This request should now succeed after reset
        mockMvc
            .perform(
                post("/api/notes")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andExpect(status().isOk())
    }
}
