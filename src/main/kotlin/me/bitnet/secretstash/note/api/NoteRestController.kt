package me.bitnet.secretstash.note.api

import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.note.dto.NoteResponse
import me.bitnet.secretstash.note.dto.PagedNoteHistoryResponse
import me.bitnet.secretstash.note.dto.PagedNoteResponse
import me.bitnet.secretstash.note.service.NoteService
import me.bitnet.secretstash.ratelimiter.RateLimit
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.RestController

@RestController
@RateLimit
class NoteRestController(
    private val noteService: NoteService,
) : NoteRestApi {
    override fun createNote(noteRequest: NoteRequest): NoteResponse = noteService.createNote(noteRequest)

    override fun getNote(noteId: NoteId): NoteResponse = noteService.getNote(noteId)

    override fun updateNote(
        noteId: NoteId,
        noteRequest: NoteRequest,
    ): NoteResponse = noteService.updateNote(noteId, noteRequest)

    override fun deleteNote(noteId: NoteId) = noteService.deleteNote(noteId)

    override fun getNotes(pageable: Pageable): PagedNoteResponse = noteService.getNotes(pageable)

    override fun getNoteHistory(
        noteId: NoteId,
        pageable: Pageable,
    ): PagedNoteHistoryResponse = noteService.getNoteHistory(noteId, pageable)
}
