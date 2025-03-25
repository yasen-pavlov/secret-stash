package me.bitnet.secretstash.note.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import me.bitnet.secretstash.note.dto.NoteRequest
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "note")
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
    var createdAt: ZonedDateTime = ZonedDateTime.now(UTC),
    @field:Column(name = "updated_at")
    var updatedAt: ZonedDateTime = ZonedDateTime.now(UTC),
    @field:Column(name = "expires_at")
    var expiresAt: ZonedDateTime? = null,
    @field:Version
    @field:Column(name = "version")
    var version: Long = 0L,
) {
    constructor(noteRequest: NoteRequest, userId: UserId) : this(
        id = UUID.randomUUID(),
        title = noteRequest.title,
        content = noteRequest.content,
        createdBy = userId,
        expiresAt = calculateExpiresAt(noteRequest.ttlMinutes),
    )

    fun update(noteRequest: NoteRequest) {
        title = noteRequest.title
        content = noteRequest.content
        updatedAt = ZonedDateTime.now(UTC)
        expiresAt = calculateExpiresAt(noteRequest.ttlMinutes)
    }

    companion object {
        private fun calculateExpiresAt(ttlMinutes: Int?): ZonedDateTime? =
            if (ttlMinutes != null && ttlMinutes > 0) {
                ZonedDateTime.now(UTC).plusMinutes(ttlMinutes.toLong())
            } else {
                null
            }
    }
}

typealias NoteId = UUID
typealias UserId = UUID
