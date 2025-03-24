package me.bitnet.secretstash.note.job

import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.exception.NoteNotFoundException
import me.bitnet.secretstash.note.infrastructure.NoteRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NoteExpirationJobTest {
    @Mock
    private lateinit var noteRepository: NoteRepository

    @Mock
    private lateinit var context: JobExecutionContext

    @Mock
    private lateinit var jobDataMap: JobDataMap

    @InjectMocks
    private lateinit var noteExpirationJob: TestableNoteExpirationJob

    private val noteId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private lateinit var note: Note

    @BeforeEach
    fun setup() {
        note =
            Note(
                id = noteId,
                title = "Test Note",
                content = "Test Content",
                createdBy = userId,
                createdAt = ZonedDateTime.now(),
                updatedAt = ZonedDateTime.now(),
            )

        whenever(context.mergedJobDataMap).thenReturn(jobDataMap)
    }

    @Test
    fun `should delete note when note id is valid and note exists`() {
        // Arrange
        whenever(jobDataMap.getString(NoteExpirationJob.NOTE_ID_KEY)).thenReturn(noteId.toString())
        whenever(noteRepository.getById(noteId)).thenReturn(note)

        // Act
        noteExpirationJob.executeInternalForTest(context)

        // Assert
        verify(noteRepository).getById(noteId)
        verify(noteRepository).delete(note)
    }

    @Test
    fun `should handle case when note not found`() {
        // Arrange
        whenever(jobDataMap.getString(NoteExpirationJob.NOTE_ID_KEY)).thenReturn(noteId.toString())
        whenever(noteRepository.getById(noteId)).thenThrow(NoteNotFoundException("Note not found"))

        // Act - should not throw
        noteExpirationJob.executeInternalForTest(context)

        // Assert
        verify(noteRepository).getById(noteId)
        verify(noteRepository, never()).delete(note)
    }

    @Test
    fun `should handle invalid UUID format`() {
        // Arrange
        whenever(jobDataMap.getString(NoteExpirationJob.NOTE_ID_KEY)).thenReturn("not-a-uuid")

        // Act
        noteExpirationJob.executeInternalForTest(context)

        // Assert
        verify(noteRepository, never()).getById(any())
        verify(noteRepository, never()).delete(any())
    }

    @Test
    fun `should handle null note id`() {
        // Arrange
        whenever(jobDataMap.getString(NoteExpirationJob.NOTE_ID_KEY)).thenReturn(null)

        // Act
        noteExpirationJob.executeInternalForTest(context)

        // Assert
        verify(noteRepository, never()).getById(any())
        verify(noteRepository, never()).delete(any())
    }

    @Test
    fun `should handle other exceptions gracefully`() {
        // Arrange
        whenever(jobDataMap.getString(NoteExpirationJob.NOTE_ID_KEY)).thenReturn(noteId.toString())
        whenever(noteRepository.getById(noteId)).thenReturn(note)
        doThrow(RuntimeException("Database error")).whenever(noteRepository).delete(note)

        // Act
        noteExpirationJob.executeInternalForTest(context)

        // Assert
        verify(noteRepository).getById(noteId)
        verify(noteRepository).delete(note)
    }

    class TestableNoteExpirationJob(
        noteRepository: NoteRepository,
    ) : NoteExpirationJob(noteRepository) {
        @Suppress("SpringTransactionalMethodCallsInspection")
        fun executeInternalForTest(context: JobExecutionContext) {
            executeInternal(context)
        }
    }
}
