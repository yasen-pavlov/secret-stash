package me.bitnet.secretstash.note.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "note_history")
class NoteHistory(
    @field:Id
    @field:Column(name = "id")
    var id: NoteHistoryId,
    @field:Column(name = "note_id")
    var noteId: NoteId,
    @field:Column(name = "title")
    var title: String,
    @field:Column(name = "content")
    var content: String,
    @field:Column(name = "updated_at")
    var updatedAt: ZonedDateTime,
    @field:Version
    @field:Column(name = "version")
    var version: Long = 0L,
) {
    constructor(note: Note) : this(
        id = UUID.randomUUID(),
        noteId = note.id,
        title = note.title,
        content = note.content,
        updatedAt = ZonedDateTime.now(),
    )
}

typealias NoteHistoryId = UUID
