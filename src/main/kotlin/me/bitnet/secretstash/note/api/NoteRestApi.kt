package me.bitnet.secretstash.note.api

import jakarta.validation.Valid
import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.note.dto.NoteResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/notes")
interface NoteRestApi {
    @PostMapping
    fun createNote(
        @Valid @RequestBody noteRequest: NoteRequest,
    ): NoteResponse

    @GetMapping("/{noteId}")
    fun getNote(
        @PathVariable noteId: NoteId,
    ): NoteResponse

    @PutMapping("/{noteId}")
    fun updateNote(
        @PathVariable noteId: NoteId,
        @Valid @RequestBody noteRequest: NoteRequest,
    ): NoteResponse

    @DeleteMapping("/{noteId}")
    fun deleteNote(
        @PathVariable noteId: NoteId,
    )
}
