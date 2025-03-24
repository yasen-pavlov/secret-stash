package me.bitnet.secretstash.note.service

import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.util.BaseIntegrationTest
import me.bitnet.secretstash.util.WithMockJwt
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.quartz.JobKey
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class NoteExpirationIntegrationTest : BaseIntegrationTest() {
    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should schedule job when creating note with expiration time`() {
        // Arrange
        val noteRequest =
            NoteRequest(
                title = "Test Self-Destruct Note",
                content = "This note will self-destruct",
                ttlMinutes = 5,
            )

        // Act
        val result =
            mockMvc
                .perform(
                    post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.expiresAt").exists())
                .andReturn()

        // Extract note ID from response
        val responseContent = result.response.contentAsString
        val noteId = objectMapper.readTree(responseContent).get("id").asText()

        // Assert - verify job was scheduled
        val jobKey = JobKey.jobKey("note-expiration-$noteId", "note-expiration-jobs")
        assertThat(scheduler.checkExists(jobKey)).isTrue()

        // Clean up
        scheduler.deleteJob(jobKey)
    }

    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should cancel job when updating note to remove expiration time`() {
        // Arrange
        val originalRequest =
            NoteRequest(
                title = "Test Note with expiration",
                content = "This note had expiration timestamp set but will be updated",
                ttlMinutes = 10,
            )

        val createResult =
            mockMvc
                .perform(
                    post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalRequest)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.expiresAt").exists())
                .andReturn()

        val noteId = objectMapper.readTree(createResult.response.contentAsString).get("id").asText()
        val jobKey = JobKey.jobKey("note-expiration-$noteId", "note-expiration-jobs")

        assertThat(scheduler.checkExists(jobKey)).isTrue()

        // Act
        val updateRequest =
            NoteRequest(
                title = "Updated Note",
                content = "This note no longer expires",
                ttlMinutes = null,
            )

        mockMvc
            .perform(
                put("/api/notes/$noteId")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(noteId))
            .andExpect(jsonPath("$.expiresAt").doesNotExist())

        // Assert
        assertThat(scheduler.checkExists(jobKey)).isFalse()
    }

    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should execute job and delete note when expiration time is reached`() {
        // Arrange
        val noteRequest =
            NoteRequest(
                title = "Expiring Note",
                content = "This note will be immediately expired",
                ttlMinutes = 1,
            )

        val createResult =
            mockMvc
                .perform(
                    post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(noteRequest.title))
                .andExpect(jsonPath("$.content").value(noteRequest.content))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andReturn()

        val noteId = objectMapper.readTree(createResult.response.contentAsString).get("id").asText()
        val noteUuid = UUID.fromString(noteId)
        val jobKey = JobKey.jobKey("note-expiration-$noteId", "note-expiration-jobs")

        val savedNote = jpaNoteRepository.findById(noteUuid)
        assertThat(savedNote).isPresent()

        // Act
        scheduler.triggerJob(jobKey)

        // Wait a short time for job to complete
        Thread.sleep(500)

        // Assert
        val deletedNote = jpaNoteRepository.findById(noteUuid)
        assertThat(deletedNote).isEmpty()
    }

    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should update job when changing expiration time`() {
        // Arrange
        val originalRequest =
            NoteRequest(
                title = "Test Note with expiration time",
                content = "This note will have its expiration time changed",
                ttlMinutes = 10,
            )

        val createResult =
            mockMvc
                .perform(
                    post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalRequest)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(originalRequest.title))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andReturn()

        val createResponseJson = objectMapper.readTree(createResult.response.contentAsString)
        val noteId = createResponseJson.get("id").asText()
        val originalExpiresAt = createResponseJson.get("expiresAt").asText()

        val jobKey = JobKey.jobKey("note-expiration-$noteId", "note-expiration-jobs")
        assertThat(scheduler.checkExists(jobKey)).isTrue()

        val originalTrigger = scheduler.getTriggersOfJob(jobKey).first()
        val originalFireTime = originalTrigger.nextFireTime

        // Act
        val updateRequest =
            NoteRequest(
                title = "Updated Note",
                content = "This note now expires sooner",
                ttlMinutes = 5,
            )

        mockMvc
            .perform(
                put("/api/notes/$noteId")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(noteId))
            .andExpect(jsonPath("$.title").value(updateRequest.title))
            .andExpect(jsonPath("$.content").value(updateRequest.content))
            .andExpect(jsonPath("$.expiresAt").exists())
            .andExpect(jsonPath("$.expiresAt").value(not(equalTo(originalExpiresAt))))

        // Assert
        assertThat(scheduler.checkExists(jobKey)).isTrue()
        val updatedTrigger = scheduler.getTriggersOfJob(jobKey).first()

        assertThat(updatedTrigger.nextFireTime).isNotEqualTo(originalFireTime)
    }
}
