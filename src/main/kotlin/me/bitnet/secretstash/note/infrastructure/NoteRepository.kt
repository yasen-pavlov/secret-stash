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
    private val noteHistoryRepository: NoteHistoryRepository,
) {
    fun getById(noteId: NoteId): Note =
        jpaNoteRepository
            .findById(noteId)
            .orElseThrow { NoteNotFoundException("Note not found") }

    fun save(note: Note): Note = jpaNoteRepository.save(note)

    fun getNotesByUser(
        userId: UserId,
        pageable: Pageable,
    ): Page<Note> = jpaNoteRepository.findByCreatedByOrderByCreatedAtDesc(userId, pageable)

    fun delete(note: Note) {
        jpaNoteRepository.delete(note)
        noteHistoryRepository.deleteByNoteId(note.id)
    }
}
