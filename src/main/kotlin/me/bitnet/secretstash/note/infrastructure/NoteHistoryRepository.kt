package me.bitnet.secretstash.note.infrastructure

import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteHistory
import me.bitnet.secretstash.note.domain.NoteId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class NoteHistoryRepository(
    private val jpaNoteHistoryRepository: JpaNoteHistoryRepository,
) {
    fun saveFromNote(note: Note): NoteHistory = jpaNoteHistoryRepository.save(NoteHistory(note))

    fun getHistoryByNoteId(
        noteId: NoteId,
        pageable: Pageable,
    ): Page<NoteHistory> = jpaNoteHistoryRepository.findByNoteIdOrderByUpdatedAtDesc(noteId, pageable)

    fun deleteByNoteId(noteId: NoteId) {
        jpaNoteHistoryRepository.deleteByNoteId(noteId)
    }
}
