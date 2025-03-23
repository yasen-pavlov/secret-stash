package me.bitnet.secretstash.note.infrastructure

import me.bitnet.secretstash.exception.DomainEntityNotFoundException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NoteRepositoryTest {
    @Mock
    private lateinit var jpaNoteRepository: JpaNoteRepository

    @InjectMocks
    private lateinit var noteRepository: NoteRepository

    @Test
    fun `should throw DomainEntityNotFoundException when note not found`() {
        // Arrange
        val noteId = UUID.randomUUID()
        `when`(jpaNoteRepository.findById(noteId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThatThrownBy {
            noteRepository.getById(noteId)
        }.isInstanceOf(DomainEntityNotFoundException::class.java)
            .hasMessage("Note not found")
    }
}
