package me.bitnet.secretstash.note.dto

import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import java.time.ZonedDateTime

data class NoteResponse(
    val id: NoteId,
    val title: String,
    val content: String,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
) {
    constructor(note: Note) : this(note.id, note.title, note.content, note.createdAt, note.updatedAt)
}
