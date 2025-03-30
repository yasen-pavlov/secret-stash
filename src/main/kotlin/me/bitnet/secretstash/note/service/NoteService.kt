package me.bitnet.secretstash.note.service

import io.github.oshai.kotlinlogging.KotlinLogging
import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.domain.UserId
import me.bitnet.secretstash.note.dto.NoteHistoryResponse
import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.note.dto.NoteResponse
import me.bitnet.secretstash.note.dto.PagedNoteHistoryResponse
import me.bitnet.secretstash.note.dto.PagedNoteResponse
import me.bitnet.secretstash.note.exception.NoteNotFoundException
import me.bitnet.secretstash.note.infrastructure.NoteHistoryRepository
import me.bitnet.secretstash.note.infrastructure.NoteRepository
import me.bitnet.secretstash.util.auth.TokenService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class NoteService(
    private val noteRepository: NoteRepository,
    private val tokenService: TokenService,
    private val noteHistoryRepository: NoteHistoryRepository,
    private val noteExpirationService: NoteExpirationService,
) {
    private val logger = KotlinLogging.logger {}
    private val maxNotes = 1000

    @PreAuthorize("hasRole('USER')")
    fun createNote(noteRequest: NoteRequest): NoteResponse {
        logger.info { "[createNote] Creating new note" }
        val note = Note(noteRequest, tokenService.getCurrentUserId())
        val savedNote = noteRepository.save(note)

        if (note.expiresAt != null) {
            noteExpirationService.scheduleNoteDeletion(savedNote)
        }

        return NoteResponse(savedNote)
    }

    @PreAuthorize("hasRole('USER')")
    fun getNotes(
        page: Int,
        size: Int,
    ): PagedNoteResponse {
        logger.info { "[getNotes] Getting notes, page: $page, size: $size" }

        val maxPageNumber = (maxNotes - 1) / size
        val adjustedPageNumber = minOf(page, maxPageNumber)

        val pageable = PageRequest.of(adjustedPageNumber, size)

        val userId = tokenService.getCurrentUserId()
        val notesPage =
            noteRepository
                .getNotesByUser(userId, pageable)
                .map { NoteResponse(it) }

        val totalElements = minOf(notesPage.totalElements, maxNotes.toLong())
        val totalPages = (totalElements + size - 1) / size

        return PagedNoteResponse(
            notesPage,
            totalElements,
            totalPages.toInt(),
        )
    }

    @PreAuthorize("hasRole('USER')")
    fun getNote(id: NoteId): NoteResponse {
        logger.info { "[getNote] Getting note with id: $id" }
        val note = noteRepository.getById(id)
        checkIfUserIdIsCurrentUser(note.createdBy)
        return NoteResponse(note)
    }

    @PreAuthorize("hasRole('USER')")
    fun getNoteHistory(
        noteId: NoteId,
        page: Int,
        size: Int,
    ): PagedNoteHistoryResponse {
        logger.info { "[getNoteHistory] Getting history for note with id: $noteId" }

        val note = noteRepository.getById(noteId)
        checkIfUserIdIsCurrentUser(note.createdBy)

        val pageable = PageRequest.of(page, size)

        val historyPage =
            noteHistoryRepository
                .getHistoryByNoteId(noteId, pageable)
                .map { NoteHistoryResponse(it) }

        return PagedNoteHistoryResponse(historyPage)
    }

    @PreAuthorize("hasRole('USER')")
    fun updateNote(
        id: NoteId,
        noteRequest: NoteRequest,
    ): NoteResponse {
        logger.info { "[updateNote] Updating note with id: $id" }
        val note = noteRepository.getById(id)
        checkIfUserIdIsCurrentUser(note.createdBy)

        if (hasNoteChanged(note, noteRequest)) {
            noteHistoryRepository.saveFromNote(note)
        }

        val oldExpiresAt = note.expiresAt

        note.update(noteRequest)

        if (oldExpiresAt != note.expiresAt) {
            noteExpirationService.scheduleNoteDeletion(note)
        }

        return NoteResponse(note)
    }

    @Suppress("kotlin:S6508")
    @PreAuthorize("hasRole('USER')")
    fun deleteNote(id: NoteId): ResponseEntity<Void> {
        logger.info { "[deleteNote] Deleting note with id: $id" }
        val note = noteRepository.getById(id)
        checkIfUserIdIsCurrentUser(note.createdBy)

        noteExpirationService.cancelScheduledDeletion(id)

        noteRepository.delete(note)
        return ResponseEntity.noContent().build()
    }

    private fun checkIfUserIdIsCurrentUser(userId: UserId) {
        if (tokenService.getCurrentUserId() != userId) {
            // throw not found here to prevent revealing that a note exists
            // if it's not owned by the currently logged-in user
            throw NoteNotFoundException("Note not found")
        }
    }

    private fun hasNoteChanged(
        originalNote: Note,
        noteRequest: NoteRequest,
    ): Boolean = originalNote.title != noteRequest.title || originalNote.content != noteRequest.content
}
