package me.bitnet.secretstash.note.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "Note request")
data class NoteRequest(
    @Schema(description = "Note title", nullable = false, required = true)
    @field:Size(min = 1, max = 255, message = "{note.title.size}")
    val title: String,
    @Schema(description = "Note content", required = true)
    @field:Size(min = 1, max = 5000, message = "{note.content.size}")
    val content: String,
)
