package me.bitnet.secretstash.note.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

@Schema(description = "Paginated Note Response")
data class PagedNoteResponse(
    @Schema(description = "List of notes")
    val content: List<NoteResponse>,
    @Schema(description = "Current page number")
    val page: Int,
    @Schema(description = "Size of the page")
    val size: Int,
    @Schema(description = "Total number of elements")
    val totalElements: Long,
    @Schema(description = "Total number of pages")
    val totalPages: Int,
    @Schema(description = "Is this the first page")
    val isFirst: Boolean,
    @Schema(description = "Is this the last page")
    val isLast: Boolean,
) {
    constructor(
        page: Page<NoteResponse>,
        totalElements: Long,
        totalPages: Int,
    ) : this(
        content = page.content,
        page = page.number,
        size = page.size,
        totalElements = totalElements,
        totalPages = totalPages,
        isFirst = page.number == 0,
        isLast = page.number >= totalPages - 1 || page.isLast,
    )
}
