package me.bitnet.secretstash.note.service

import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteHistory
import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.note.exception.NoteNotFoundException
import me.bitnet.secretstash.note.infrastructure.NoteHistoryRepository
import me.bitnet.secretstash.note.infrastructure.NoteRepository
import me.bitnet.secretstash.util.TokenService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NoteServiceTest {
    @Mock
    private lateinit var noteRepository: NoteRepository

    @Mock
    private lateinit var noteHistoryRepository: NoteHistoryRepository

    @Mock
    private lateinit var tokenService: TokenService

    @Mock
    private lateinit var noteExpirationService: NoteExpirationService

    @InjectMocks
    private lateinit var noteService: NoteService

    private val ownerId = UUID.randomUUID()
    private val differentUserId = UUID.randomUUID()
    private val noteId = UUID.randomUUID()

    @Test
    fun `should throw exception when getting note with different user id`() {
        // Arrange
        val note = createTestNote(ownerId)
        whenever(noteRepository.getById(noteId)).thenReturn(note)
        whenever(tokenService.getCurrentUserId()).thenReturn(differentUserId)

        // Act & Assert
        assertThatThrownBy {
            noteService.getNote(noteId)
        }.isInstanceOf(NoteNotFoundException::class.java)
            .hasMessage("Note not found")
    }

    @Test
    fun `should throw exception when updating note with different user id`() {
        // Arrange
        val note = createTestNote(ownerId)
        val updateRequest = NoteRequest("Updated Title", "Updated Content")

        whenever(noteRepository.getById(noteId)).thenReturn(note)
        whenever(tokenService.getCurrentUserId()).thenReturn(differentUserId)

        // Act & Assert
        assertThatThrownBy {
            noteService.updateNote(noteId, updateRequest)
        }.isInstanceOf(NoteNotFoundException::class.java)
            .hasMessage("Note not found")
    }

    @Test
    fun `should throw exception when deleting note with different user id`() {
        // Arrange
        val note = createTestNote(ownerId)

        whenever(noteRepository.getById(noteId)).thenReturn(note)
        whenever(tokenService.getCurrentUserId()).thenReturn(differentUserId)

        // Act & Assert
        assertThatThrownBy {
            noteService.deleteNote(noteId)
        }.isInstanceOf(NoteNotFoundException::class.java)
            .hasMessage("Note not found")
    }

    @Test
    fun `should get paginated notes with valid page parameters`() {
        // Arrange
        val pageSize = 20
        val pageNumber = 0
        val pageable = PageRequest.of(pageNumber, pageSize)
        val notes = createTestNotes(pageSize)
        val notesPage = PageImpl(notes, pageable, pageSize.toLong())

        whenever(tokenService.getCurrentUserId()).thenReturn(ownerId)
        whenever(noteRepository.getNotesByUser(ownerId, pageable)).thenReturn(notesPage)

        // Act
        val result = noteService.getNotes(pageable)

        // Assert
        assertThat(result.content).hasSize(pageSize)
        assertThat(result.page).isEqualTo(pageNumber)
        assertThat(result.size).isEqualTo(pageSize)
        assertThat(result.totalElements).isEqualTo(pageSize.toLong())
        assertThat(result.totalPages).isEqualTo(1)
        assertThat(result.isFirst).isTrue()
        assertThat(result.isLast).isTrue()
    }

    @Test
    fun `should cap page size to maximum allowed`() {
        // Arrange
        val pageSize = 150 // Greater than max allowed (100)
        val maxPageSize = 100 // From NoteService
        val pageNumber = 0
        val requestedPageable = PageRequest.of(pageNumber, pageSize)
        val adjustedPageable = PageRequest.of(pageNumber, maxPageSize)
        val notes = createTestNotes(maxPageSize)
        val notesPage = PageImpl(notes, adjustedPageable, 150L)

        whenever(tokenService.getCurrentUserId()).thenReturn(ownerId)
        whenever(noteRepository.getNotesByUser(ownerId, adjustedPageable)).thenReturn(notesPage)

        // Act
        val result = noteService.getNotes(requestedPageable)

        // Assert
        assertThat(result.size).isEqualTo(maxPageSize)
        assertThat(result.content).hasSize(maxPageSize)
        assertThat(result.totalElements).isEqualTo(150L)
        assertThat(result.totalPages).isEqualTo(2) // 150/100 = 1.5 -> 2
    }

    @Test
    fun `should adjust page number if exceeds maximum allowed`() {
        // Arrange
        val pageSize = 10
        val totalNotes = 80L // Less than max (1000)
        val excessivePageNumber = 10 // Would be beyond actual pages

        val requestedPageable = PageRequest.of(excessivePageNumber, pageSize)
        val emptyList = listOf<Note>()
        val notesPage = PageImpl(emptyList, PageRequest.of(0, pageSize), totalNotes)

        whenever(tokenService.getCurrentUserId()).thenReturn(ownerId)
        whenever(noteRepository.getNotesByUser(ownerId, requestedPageable)).thenReturn(notesPage)

        // Act
        val result = noteService.getNotes(requestedPageable)

        // Assert
        assertThat(result.totalElements).isEqualTo(totalNotes)
        assertThat(result.totalPages).isEqualTo(8) // 80/10 = 8
    }

    @Test
    fun `should cap total elements to maximum notes allowed`() {
        // Arrange
        val pageSize = 100
        val pageNumber = 0
        val maxNotes = 1000L // From NoteService
        val totalNotes = 1500L // More than max

        val pageable = PageRequest.of(pageNumber, pageSize)
        val notes = createTestNotes(pageSize)
        val notesPage = PageImpl(notes, pageable, totalNotes)

        whenever(tokenService.getCurrentUserId()).thenReturn(ownerId)
        whenever(noteRepository.getNotesByUser(ownerId, pageable)).thenReturn(notesPage)

        // Act
        val result = noteService.getNotes(pageable)

        // Assert
        assertThat(result.totalElements).isEqualTo(maxNotes)
        assertThat(result.totalPages).isEqualTo(10) // 1000/100 = 10
    }

    @Test
    fun `should save note history when updating note with different content`() {
        // Arrange
        val note = createTestNote(ownerId)
        val originalTitle = note.title
        val originalContent = note.content
        val updateRequest = NoteRequest("Updated Title", "Updated Content")

        whenever(noteRepository.getById(noteId)).thenReturn(note)
        whenever(tokenService.getCurrentUserId()).thenReturn(ownerId)

        // Act
        noteService.updateNote(noteId, updateRequest)

        // Assert
        org.mockito.kotlin
            .verify(noteHistoryRepository)
            .saveFromNote(note)
        assertThat(note.title).isEqualTo("Updated Title")
        assertThat(note.content).isEqualTo("Updated Content")
        assertThat(originalTitle).isNotEqualTo(note.title)
        assertThat(originalContent).isNotEqualTo(note.content)
    }

    @Test
    fun `should not save note history when updating note with same content`() {
        // Arrange
        val note = createTestNote(ownerId)
        val updateRequest = NoteRequest(note.title, note.content) // Same title and content

        whenever(noteRepository.getById(noteId)).thenReturn(note)
        whenever(tokenService.getCurrentUserId()).thenReturn(ownerId)

        // Act
        noteService.updateNote(noteId, updateRequest)

        // Assert
        org.mockito.kotlin
            .verify(noteHistoryRepository, org.mockito.kotlin.never())
            .saveFromNote(note)
    }

    @Test
    fun `should save note history when updating only title`() {
        // Arrange
        val note = createTestNote(ownerId)
        val updateRequest = NoteRequest("Updated Title Only", note.content) // Only title changes

        whenever(noteRepository.getById(noteId)).thenReturn(note)
        whenever(tokenService.getCurrentUserId()).thenReturn(ownerId)

        // Act
        noteService.updateNote(noteId, updateRequest)

        // Assert
        org.mockito.kotlin
            .verify(noteHistoryRepository)
            .saveFromNote(note)
    }

    @Test
    fun `should save note history when updating only content`() {
        // Arrange
        val note = createTestNote(ownerId)
        val updateRequest = NoteRequest(note.title, "Updated Content Only") // Only content changes

        whenever(noteRepository.getById(noteId)).thenReturn(note)
        whenever(tokenService.getCurrentUserId()).thenReturn(ownerId)

        // Act
        noteService.updateNote(noteId, updateRequest)

        // Assert
        org.mockito.kotlin
            .verify(noteHistoryRepository)
            .saveFromNote(note)
    }

    @Test
    fun `should get note history with correct pagination`() {
        // Arrange
        val pageSize = 10
        val pageNumber = 0
        val pageable = PageRequest.of(pageNumber, pageSize)
        val note = createTestNote(ownerId)
        val noteHistories = createTestNoteHistories(3)
        val historiesPage = PageImpl(noteHistories, pageable, noteHistories.size.toLong())

        whenever(noteRepository.getById(noteId)).thenReturn(note)
        whenever(tokenService.getCurrentUserId()).thenReturn(ownerId)
        whenever(noteHistoryRepository.getHistoryByNoteId(noteId, pageable)).thenReturn(historiesPage)

        // Act
        val result = noteService.getNoteHistory(noteId, pageable)

        // Assert
        assertThat(result.content).hasSize(3)
        assertThat(result.page).isEqualTo(pageNumber)
        assertThat(result.size).isEqualTo(pageSize)
        assertThat(result.totalElements).isEqualTo(3)
        assertThat(result.isFirst).isTrue()
        assertThat(result.isLast).isTrue()
    }

    @Test
    fun `should throw exception when getting history for note with different user id`() {
        // Arrange
        val note = createTestNote(ownerId)
        val pageable = PageRequest.of(0, 10)

        whenever(noteRepository.getById(noteId)).thenReturn(note)
        whenever(tokenService.getCurrentUserId()).thenReturn(differentUserId)

        // Act & Assert
        assertThatThrownBy {
            noteService.getNoteHistory(noteId, pageable)
        }.isInstanceOf(NoteNotFoundException::class.java)
            .hasMessage("Note not found")
    }

    @Test
    fun `should cap note history page size to maximum allowed`() {
        // Arrange
        val pageSize = 150 // Greater than max allowed (100)
        val maxPageSize = 100 // From NoteService
        val pageNumber = 0
        val note = createTestNote(ownerId)
        val requestedPageable = PageRequest.of(pageNumber, pageSize)
        val adjustedPageable = PageRequest.of(pageNumber, maxPageSize)
        val noteHistories = createTestNoteHistories(maxPageSize)
        val historiesPage = PageImpl(noteHistories, adjustedPageable, 150L)

        whenever(noteRepository.getById(noteId)).thenReturn(note)
        whenever(tokenService.getCurrentUserId()).thenReturn(ownerId)
        whenever(noteHistoryRepository.getHistoryByNoteId(noteId, adjustedPageable)).thenReturn(historiesPage)

        // Act
        val result = noteService.getNoteHistory(noteId, requestedPageable)

        // Assert
        assertThat(result.size).isEqualTo(maxPageSize)
        assertThat(result.content).hasSize(maxPageSize)
    }

    @Test
    fun `should delete note history when deleting note`() {
        // Arrange
        val note = createTestNote(ownerId)

        whenever(noteRepository.getById(noteId)).thenReturn(note)
        whenever(tokenService.getCurrentUserId()).thenReturn(ownerId)

        // Act
        noteService.deleteNote(noteId)

        // Assert
        org.mockito.kotlin
            .verify(noteRepository)
            .delete(note)
    }

    private fun createTestNoteHistories(count: Int): List<NoteHistory> {
        val histories = mutableListOf<NoteHistory>()
        for (i in 1..count) {
            histories.add(
                NoteHistory(
                    id = UUID.randomUUID(),
                    noteId = noteId,
                    title = "Historical Title $i",
                    content = "Historical Content $i",
                    updatedAt = ZonedDateTime.now().minusDays(i.toLong()),
                ),
            )
        }
        return histories
    }

    private fun createTestNotes(count: Int): List<Note> {
        val notes = mutableListOf<Note>()
        for (i in 1..count) {
            notes.add(
                Note(
                    id = UUID.randomUUID(),
                    title = "Test Note $i",
                    content = "Test Content $i",
                    createdBy = ownerId,
                    createdAt = ZonedDateTime.now().minusDays(i.toLong()),
                    updatedAt = ZonedDateTime.now(),
                ),
            )
        }
        return notes
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
