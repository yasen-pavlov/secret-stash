package me.bitnet.secretstash.note.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

@Schema(description = "Note request")
data class NoteRequest(
    @Schema(description = "Note title", required = true)
    @field:Size(min = 1, max = 255, message = "{note.title.size}")
    val title: String,
    @Schema(description = "Note content", required = true)
    @field:Size(min = 1, max = 5000, message = "{note.content.size}")
    val content: String,
    @Schema(description = "Number of minutes after which the note is deleted", nullable = true, required = false)
    @field:Min(value = 0, message = "{note.ttl-minutes.min}")
    @field:Max(value = 10080, message = "{note.ttl-minutes.max}")
    val ttlMinutes: Int? = null,
)
