package me.bitnet.secretstash.note.dto

import io.swagger.v3.oas.annotations.media.Schema
import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import java.time.ZonedDateTime

@Schema(description = "Note")
data class NoteResponse(
    @Schema(description = "Note id")
    val id: NoteId,
    @Schema(description = "Note title")
    val title: String,
    @Schema(description = "Note content")
    val content: String,
    @Schema(description = "Created date")
    val createdAt: ZonedDateTime,
    @Schema(description = "Modification date")
    val updatedAt: ZonedDateTime,
    @Schema(description = "Timestamp when the note will expire (null if no expiration is set)")
    val expiresAt: ZonedDateTime?,
) {
    constructor(note: Note) : this(
        note.id,
        note.title,
        note.content,
        note.createdAt,
        note.updatedAt,
        note.expiresAt,
    )
}
