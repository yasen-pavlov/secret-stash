package me.bitnet.secretstash.note.infrastructure

import me.bitnet.secretstash.note.domain.NoteHistory
import me.bitnet.secretstash.note.domain.NoteHistoryId
import me.bitnet.secretstash.note.domain.NoteId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface JpaNoteHistoryRepository : JpaRepository<NoteHistory, NoteHistoryId> {
    fun findByNoteIdOrderByUpdatedAtDesc(
        noteId: NoteId,
        pageable: Pageable,
    ): Page<NoteHistory>

    fun deleteByNoteId(noteId: NoteId)
}
