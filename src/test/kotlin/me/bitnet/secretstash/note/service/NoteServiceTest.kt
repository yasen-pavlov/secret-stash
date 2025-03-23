package me.bitnet.secretstash.note.service

import me.bitnet.secretstash.exception.DomainEntityNotFoundException
import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.note.infrastructure.NoteRepository
import me.bitnet.secretstash.util.TokenService
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NoteServiceTest {
    @Mock
    private lateinit var noteRepository: NoteRepository

    @Mock
    private lateinit var tokenService: TokenService

    @InjectMocks
    private lateinit var noteService: NoteService

    private val ownerId = UUID.randomUUID()
    private val differentUserId = UUID.randomUUID()
    private val noteId = UUID.randomUUID()

    @Test
    fun `should throw exception when getting note with different user id`() {
        // Arrange
        val note = createTestNote(ownerId)
        `when`(noteRepository.getById(noteId)).thenReturn(note)
        `when`(tokenService.getCurrentUserId()).thenReturn(differentUserId)

        // Act & Assert
        assertThatThrownBy {
            noteService.getNote(noteId)
        }.isInstanceOf(DomainEntityNotFoundException::class.java)
            .hasMessage("Note not found")
    }

    @Test
    fun `should throw exception when updating note with different user id`() {
        // Arrange
        val note = createTestNote(ownerId)
        val updateRequest = NoteRequest("Updated Title", "Updated Content")

        `when`(noteRepository.getById(noteId)).thenReturn(note)
        `when`(tokenService.getCurrentUserId()).thenReturn(differentUserId)

        // Act & Assert
        assertThatThrownBy {
            noteService.updateNote(noteId, updateRequest)
        }.isInstanceOf(DomainEntityNotFoundException::class.java)
            .hasMessage("Note not found")
    }

    @Test
    fun `should throw exception when deleting note with different user id`() {
        // Arrange
        val note = createTestNote(ownerId)

        `when`(noteRepository.getById(noteId)).thenReturn(note)
        `when`(tokenService.getCurrentUserId()).thenReturn(differentUserId)

        // Act & Assert
        assertThatThrownBy {
            noteService.deleteNote(noteId)
        }.isInstanceOf(DomainEntityNotFoundException::class.java)
            .hasMessage("Note not found")
    }

    private fun createTestNote(userId: UUID): Note =
        Note(
            id = noteId,
            title = "Test Note",
            content = "Test Content",
            createdBy = userId,
            createdAt = ZonedDateTime.now(),
            updatedAt = ZonedDateTime.now(),
        )
}
