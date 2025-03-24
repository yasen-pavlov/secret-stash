package me.bitnet.secretstash.note.infrastructure

import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.domain.UserId
import me.bitnet.secretstash.note.exception.NoteNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class NoteRepository(
    private val jpaNoteRepository: JpaNoteRepository,
) {
    fun getById(noteId: NoteId): Note =
        jpaNoteRepository
            .findById(noteId)
            .orElseThrow { NoteNotFoundException("Note not found") }

    fun save(note: Note): Note = jpaNoteRepository.save(note)

    fun delete(note: Note): Unit = jpaNoteRepository.delete(note)

    fun getNotesByUser(
        userId: UserId,
        pageable: Pageable,
    ): Page<Note> = jpaNoteRepository.findAllByCreatedByOrderByCreatedAtDesc(userId, pageable)
}
