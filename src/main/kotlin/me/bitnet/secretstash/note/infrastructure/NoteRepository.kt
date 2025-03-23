package me.bitnet.secretstash.note.infrastructure

import me.bitnet.secretstash.exception.DomainEntityNotFoundException
import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import org.springframework.stereotype.Repository

@Repository
class NoteRepository(
    private val jpaNoteRepository: JpaNoteRepository,
) {
    fun getById(noteId: NoteId): Note =
        jpaNoteRepository
            .findById(noteId)
            .orElseThrow { DomainEntityNotFoundException("Note not found") }

    fun save(note: Note): Note = jpaNoteRepository.save(note)

    fun delete(note: Note): Unit = jpaNoteRepository.delete(note)
}
