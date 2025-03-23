package me.bitnet.secretstash.note.service

import io.github.oshai.kotlinlogging.KotlinLogging
import me.bitnet.secretstash.exception.DomainEntityNotFoundException
import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.domain.UserId
import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.note.dto.NoteResponse
import me.bitnet.secretstash.note.infrastructure.NoteRepository
import me.bitnet.secretstash.util.TokenService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class NoteService(
    private val noteRepository: NoteRepository,
    private val tokenService: TokenService,
) {
    private val logger = KotlinLogging.logger {}

    @PreAuthorize("hasRole('USER')")
    fun createNote(noteRequest: NoteRequest): NoteResponse {
        logger.info { "[createNote] Creating new note" }
        val note = noteRepository.save(Note(noteRequest, tokenService.getCurrentUserId()))
        return NoteResponse(note)
    }

    @PreAuthorize("hasRole('USER')")
    fun getNote(id: NoteId): NoteResponse {
        logger.info { "[getNote] Getting note with id: $id" }
        val note = noteRepository.getById(id)
        checkIfUserIdIsCurrentUser(note.createdBy)
        return NoteResponse(note)
    }

    @PreAuthorize("hasRole('USER')")
    fun updateNote(
        id: NoteId,
        noteRequest: NoteRequest,
    ): NoteResponse {
        logger.info { "[updateNote] Updating note with id: $id" }
        val note = noteRepository.getById(id)
        checkIfUserIdIsCurrentUser(note.createdBy)
        note.update(noteRequest)
        return NoteResponse(note)
    }

    @Suppress("kotlin:S6508")
    @PreAuthorize("hasRole('USER')")
    fun deleteNote(id: NoteId): ResponseEntity<Void> {
        logger.info { "[deleteNote] Deleting note with id: $id" }
        val note = noteRepository.getById(id)
        checkIfUserIdIsCurrentUser(note.createdBy)
        noteRepository.delete(note)
        return ResponseEntity.noContent().build()
    }

    private fun checkIfUserIdIsCurrentUser(userId: UserId) {
        if (tokenService.getCurrentUserId() != userId) {
            // throw not found here to prevent revealing that a note exists
            // if it's not owned by the currently logged-in user
            throw DomainEntityNotFoundException("Note not found")
        }
    }
}
