package me.bitnet.secretstash.note.dto

import io.swagger.v3.oas.annotations.media.Schema
import me.bitnet.secretstash.note.domain.NoteHistory
import java.time.ZonedDateTime

@Schema(description = "Note History Entry")
data class NoteHistoryResponse(
    @Schema(description = "Note title at time of update")
    val title: String,
    @Schema(description = "Note content at time of update")
    val content: String,
    @Schema(description = "Update timestamp")
    val updatedAt: ZonedDateTime,
) {
    constructor(noteHistory: NoteHistory) : this(
        noteHistory.title,
        noteHistory.content,
        noteHistory.updatedAt,
    )
}
