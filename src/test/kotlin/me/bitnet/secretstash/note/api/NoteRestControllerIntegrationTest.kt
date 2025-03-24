package me.bitnet.secretstash.note.api

import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.util.BaseIntegrationTest
import me.bitnet.secretstash.util.WithMockJwt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.ZonedDateTime
import java.util.UUID

class NoteRestControllerIntegrationTest : BaseIntegrationTest() {
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
    fun `should get paginated notes when user has USER role`() {
        // Arrange
        val numberOfNotes = 5
        val noteIds = mutableListOf<NoteId>()

        for (i in 1..numberOfNotes) {
            val noteRequest =
                NoteRequest(
                    title = "Test Note $i",
                    content = "This is test note content $i",
                )
            noteIds.add(createTestNote(noteRequest))
        }

        // Act & Assert
        // First page with size 3
        mockMvc
            .perform(
                get("/api/notes?page=0&size=3&sort=createdAt,desc")
                    .with(csrf()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(3))
            .andExpect(jsonPath("$.totalElements").isNumber)
            .andExpect(jsonPath("$.totalPages").isNumber)
            .andExpect(jsonPath("$.isFirst").value(true))
            .andExpect(jsonPath("$.isLast").value(false))

        // Second page with size 3 (should contain remaining 2 notes)
        mockMvc
            .perform(
                get("/api/notes?page=1&size=3&sort=createdAt,desc")
                    .with(csrf()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(3))
            .andExpect(jsonPath("$.isFirst").value(false))
            .andExpect(jsonPath("$.isLast").value(true))
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
            ).andExpect(status().isNoContent)

        // Assert
        val deletedNote = jpaNoteRepository.findById(noteId).orElse(null)
        assertThat(deletedNote).isNull()
    }

    @Test
    fun `should not access note endpoints when not authenticated`() {
        // Arrange
        val noteId = UUID.randomUUID()

        // Act & Assert
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

        // Act & Assert
        mockMvc
            .perform(
                get("/api/notes/$noteId")
                    .with(csrf()),
            ).andExpect(status().isForbidden)
    }

    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should return multiple validation errors when both title and content are invalid`() {
        // Arrange
        val invalidNoteRequest =
            NoteRequest(
                title = "",
                content = "",
            )

        // Act & Assert
        mockMvc
            .perform(
                post("/api/notes")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidNoteRequest)),
            ).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.title").value("Title must be between 1 and 255 characters"))
            .andExpect(jsonPath("$.errors.content").value("Content must be between 1 and 5000 characters"))
    }

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
