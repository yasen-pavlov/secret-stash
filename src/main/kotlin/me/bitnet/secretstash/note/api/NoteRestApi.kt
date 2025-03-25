package me.bitnet.secretstash.note.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.note.dto.NoteResponse
import me.bitnet.secretstash.note.dto.PagedNoteHistoryResponse
import me.bitnet.secretstash.note.dto.PagedNoteResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Notes", description = "Notes related resources")
@RequestMapping("/api/notes")
interface NoteRestApi {
    @PostMapping
    @Operation(summary = "Create note", description = "Creates a new note by providing a title and content")
    fun createNote(
        @Valid @RequestBody noteRequest: NoteRequest,
    ): NoteResponse

    @GetMapping
    @Operation(
        summary = "Get notes",
        description = "Gets all notes for the current user sorted by creation date, limited to latest 1000 notes",
    )
    fun getNotes(
        @Parameter(description = "Page number (0-based)", required = false)
        @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "{pagination.page.min}")
        page: Int,
        @Parameter(description = "Page size (max 100)", required = false)
        @RequestParam(defaultValue = "20")
        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 100, message = "{pagination.size.max}")
        size: Int,
    ): PagedNoteResponse

    @GetMapping("/{noteId}")
    @Operation(summary = "Get note", description = "Gets specific note by id")
    fun getNote(
        @Parameter(description = "Note id", required = true)
        @PathVariable noteId: NoteId,
    ): NoteResponse

    @GetMapping("/{noteId}/history")
    @Operation(
        summary = "Get note history",
        description = "Gets history of changes for a specific note sorted by update date",
    )
    fun getNoteHistory(
        @Parameter(description = "Note id", required = true)
        @PathVariable noteId: NoteId,
        @Parameter(description = "Page number (0-based)", required = false)
        @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "{pagination.page.min}")
        page: Int,
        @Parameter(description = "Page size (max 100)", required = false)
        @RequestParam(defaultValue = "20")
        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 100, message = "{pagination.size.max}")
        size: Int,
    ): PagedNoteHistoryResponse

    @PutMapping("/{noteId}")
    @Operation(summary = "Update note", description = "Updates a note by id by providing a title and content")
    fun updateNote(
        @Parameter(description = "Note id", required = true)
        @PathVariable noteId: NoteId,
        @Valid @RequestBody noteRequest: NoteRequest,
    ): NoteResponse

    @DeleteMapping("/{noteId}")
    @Operation(
        summary = "Delete note",
        description = "Deletes a note by id",
        responses = [ApiResponse(responseCode = "204", description = "Note deleted")],
    )
    fun deleteNote(
        @Parameter(description = "Note id", required = true)
        @PathVariable noteId: NoteId,
    ): ResponseEntity<Void>
}
