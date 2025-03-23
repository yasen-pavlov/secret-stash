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
@Suppress("JpaDataSourceORMInspection")
class Note(
    @field:Id
    @field:Column(name = "id")
    var id: NoteId,
    @field:Column(name = "title")
    var title: String,
    @field:Column(name = "content")
    var content: String,
    @field:Column(name = "created_by")
    var createdBy: UserId,
    @field:Column(name = "created_at")
    @field:CreatedDate
    var createdAt: ZonedDateTime,
    @field:Column(name = "updated_at")
    @field:LastModifiedDate
    var updatedAt: ZonedDateTime,
    @field:Version
    @field:Column(name = "version")
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
