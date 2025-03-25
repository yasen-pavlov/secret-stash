package me.bitnet.secretstash.note.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

@Schema(description = "Paginated Note History Response")
data class PagedNoteHistoryResponse(
    @Schema(description = "List of note history entries")
    val content: List<NoteHistoryResponse>,
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
        page: Page<NoteHistoryResponse>,
    ) : this(
        content = page.content,
        page = page.number,
        size = page.size,
        totalElements = page.totalElements,
        totalPages = page.totalPages,
        isFirst = page.isFirst,
        isLast = page.isLast,
    )
}
