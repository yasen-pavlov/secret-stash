package me.bitnet.secretstash.note.infrastructure

import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.domain.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface JpaNoteRepository : JpaRepository<Note, NoteId> {
    fun findByCreatedByOrderByCreatedAtDesc(
        userId: UserId,
        pageable: Pageable,
    ): Page<Note>
}
