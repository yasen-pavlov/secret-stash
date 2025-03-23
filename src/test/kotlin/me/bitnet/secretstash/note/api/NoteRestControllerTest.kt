package me.bitnet.secretstash.note.api

import com.fasterxml.jackson.databind.ObjectMapper
import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.note.infrastructure.JpaNoteRepository
import me.bitnet.secretstash.util.TestcontainersConfiguration
import me.bitnet.secretstash.util.WithMockJwt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.ZonedDateTime
import java.util.UUID

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@AutoConfigureMockMvc
class NoteRestControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var jpaNoteRepository: JpaNoteRepository

    // User ID matching the one in WithMockJwt
    private val testUserId = UUID.fromString("0c47a356-edb2-47ae-923c-9f2902c622be")

    @AfterEach
    fun cleanup() {
        jpaNoteRepository.deleteAll()
    }

    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should create a note when user has USER role`() {
        // Arrange
        val noteRequest =
            NoteRequest(
                title = "Test Note",
                content = "This is a test note content",
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
                .andExpect(jsonPath("$.title").value(noteRequest.title))
                .andExpect(jsonPath("$.content").value(noteRequest.content))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn()

        // Assert
        val responseContent = result.response.contentAsString
        val noteId = objectMapper.readTree(responseContent).get("id").asText()

        val savedNote = jpaNoteRepository.findById(UUID.fromString(noteId)).orElse(null)
        assertThat(savedNote).isNotNull
        assertThat(savedNote.title).isEqualTo(noteRequest.title)
        assertThat(savedNote.content).isEqualTo(noteRequest.content)
        assertThat(savedNote.createdBy).isEqualTo(testUserId)
    }

    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should get a note when user has USER role and is the creator`() {
        // Arrange
        val noteRequest =
            NoteRequest(
                title = "Test Note for Get",
                content = "This is a test note content for get operation",
            )

        val noteId = createTestNote(noteRequest)

        // Act & Assert
        mockMvc
            .perform(
                get("/api/notes/$noteId")
                    .with(csrf()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(noteId.toString()))
            .andExpect(jsonPath("$.title").value(noteRequest.title))
            .andExpect(jsonPath("$.content").value(noteRequest.content))
    }

    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should update a note when user has USER role and is the creator`() {
        // Arrange
        val originalNoteRequest =
            NoteRequest(
                title = "Original Title",
                content = "Original Content",
            )

        val noteId = createTestNote(originalNoteRequest)

        val updateNoteRequest =
            NoteRequest(
                title = "Updated Title",
                content = "Updated Content",
            )

        // Act
        mockMvc
            .perform(
                put("/api/notes/$noteId")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateNoteRequest)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(noteId.toString()))
            .andExpect(jsonPath("$.title").value(updateNoteRequest.title))
            .andExpect(jsonPath("$.content").value(updateNoteRequest.content))

        // Assert
        val updatedNote = jpaNoteRepository.findById(noteId).orElse(null)
        assertThat(updatedNote).isNotNull
        assertThat(updatedNote.title).isEqualTo(updateNoteRequest.title)
        assertThat(updatedNote.content).isEqualTo(updateNoteRequest.content)
        assertThat(updatedNote.createdBy).isEqualTo(testUserId)
    }

    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should delete a note when user has USER role and is the creator`() {
        // Arrange
        val noteRequest =
            NoteRequest(
                title = "Note to Delete",
                content = "This note will be deleted",
            )

        val noteId = createTestNote(noteRequest)

        // Act
        mockMvc
            .perform(
                delete("/api/notes/$noteId")
                    .with(csrf()),
            ).andExpect(status().isOk)

        // Assert
        val deletedNote = jpaNoteRepository.findById(noteId).orElse(null)
        assertThat(deletedNote).isNull()
    }

    @Test
    fun `should not access note endpoints when not authenticated`() {
        // Arrange
        val noteId = UUID.randomUUID()

        // Act & Assert - use only GET to test unauthenticated access
        mockMvc
            .perform(
                get("/api/notes/$noteId"),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockJwt(roles = ["OTHER"])
    fun `should not access note endpoints when user has wrong role`() {
        // Arrange
        val noteId = UUID.randomUUID()

        // Act & Assert - use GET to test wrong role
        mockMvc
            .perform(
                get("/api/notes/$noteId")
                    .with(csrf()),
            ).andExpect(status().isForbidden)
    }

    /**
     * Helper method to create a test note directly via the repository
     */
    private fun createTestNote(noteRequest: NoteRequest): NoteId {
        val note =
            Note(
                id = UUID.randomUUID(),
                title = noteRequest.title,
                content = noteRequest.content,
                createdBy = testUserId,
                createdAt = ZonedDateTime.now(),
                updatedAt = ZonedDateTime.now(),
            )

        val savedNote = jpaNoteRepository.save(note)
        return savedNote.id
    }
}
