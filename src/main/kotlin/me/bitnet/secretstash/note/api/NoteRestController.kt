package me.bitnet.secretstash.note.api

import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.note.dto.NoteResponse
import me.bitnet.secretstash.note.dto.PagedNoteHistoryResponse
import me.bitnet.secretstash.note.dto.PagedNoteResponse
import me.bitnet.secretstash.note.service.NoteService
import me.bitnet.secretstash.util.ratelimiter.RateLimit
import org.springframework.web.bind.annotation.RestController

@RestController
@RateLimit
class NoteRestController(
    private val noteService: NoteService,
) : NoteRestApi {
    override fun createNote(noteRequest: NoteRequest): NoteResponse = noteService.createNote(noteRequest)

    override fun getNotes(
        page: Int,
        size: Int,
    ): PagedNoteResponse = noteService.getNotes(page, size)

    override fun getNote(noteId: NoteId): NoteResponse = noteService.getNote(noteId)

    override fun getNoteHistory(
        noteId: NoteId,
        page: Int,
        size: Int,
    ): PagedNoteHistoryResponse = noteService.getNoteHistory(noteId, page, size)

    override fun updateNote(
        noteId: NoteId,
        noteRequest: NoteRequest,
    ): NoteResponse = noteService.updateNote(noteId, noteRequest)

    override fun deleteNote(noteId: NoteId) = noteService.deleteNote(noteId)
}
