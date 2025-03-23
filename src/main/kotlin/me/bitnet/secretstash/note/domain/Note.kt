package me.bitnet.secretstash.note.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import me.bitnet.secretstash.note.dto.NoteRequest
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "note")
@EntityListeners(AuditingEntityListener::class)
class Note(
    @Id
    @Column(name = "id")
    var id: NoteId,
    @Column(name = "title")
    var title: String,
    @Column(name = "content")
    var content: String,
    @Column(name = "created_by")
    var createdBy: UserId,
    @Column(name = "created_at")
    @CreatedDate
    var createdAt: ZonedDateTime,
    @Column(name = "updated_at")
    @LastModifiedDate
    var updatedAt: ZonedDateTime,
    @Version
    @Column(name = "version")
    var version: Long = 0L,
) {
    constructor(noteRequest: NoteRequest, userId: UserId) : this(
        id = UUID.randomUUID(),
        title = noteRequest.title,
        content = noteRequest.content,
        createdBy = userId,
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now(),
    )

    fun update(noteRequest: NoteRequest) {
        title = noteRequest.title
        content = noteRequest.content
    }
}

typealias NoteId = UUID
typealias UserId = UUID
