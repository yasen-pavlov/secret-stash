package me.bitnet.secretstash.note.infrastructure

import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.domain.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaNoteRepository : JpaRepository<Note, NoteId> {
    @Query("SELECT n FROM Note n WHERE n.createdBy = :userId ORDER BY n.createdAt DESC")
    fun findAllByCreatedByOrderByCreatedAtDesc(
        userId: UserId,
        pageable: Pageable,
    ): Page<Note>
}
