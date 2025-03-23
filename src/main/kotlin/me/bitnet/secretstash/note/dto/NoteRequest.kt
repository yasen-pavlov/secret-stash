package me.bitnet.secretstash.note.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class NoteRequest(
    @field:NotEmpty(message = "{note.title.empty}")
    @field:Size(max = 255, message = "{note.title.max}")
    val title: String,
    @field:NotEmpty(message = "{note.content.empty}")
    @field:Size(max = 5000, message = "{note.content.max}")
    val content: String,
)
