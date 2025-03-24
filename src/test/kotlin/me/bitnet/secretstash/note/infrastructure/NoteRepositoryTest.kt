package me.bitnet.secretstash.note.infrastructure

import me.bitnet.secretstash.note.exception.NoteNotFoundException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NoteRepositoryTest {
    @Mock
    private lateinit var jpaNoteRepository: JpaNoteRepository

    @InjectMocks
    private lateinit var noteRepository: NoteRepository

    @Test
    fun `should throw NoteNoteFoundException when note not found`() {
        // Arrange
        val noteId = UUID.randomUUID()
        whenever(jpaNoteRepository.findById(noteId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThatThrownBy {
            noteRepository.getById(noteId)
        }.isInstanceOf(NoteNotFoundException::class.java)
            .hasMessage("Note not found")
    }
}
