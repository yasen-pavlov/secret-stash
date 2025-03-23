package me.bitnet.secretstash.note.service

import me.bitnet.secretstash.exception.DomainEntityNotFoundException
import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.domain.UserId
import me.bitnet.secretstash.note.dto.NoteRequest
import me.bitnet.secretstash.note.dto.NoteResponse
import me.bitnet.secretstash.note.infrastructure.NoteRepository
import me.bitnet.secretstash.util.TokenService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class NoteService(
    private val noteRepository: NoteRepository,
    private val tokenService: TokenService,
) {
    @PreAuthorize("hasRole('USER')")
    fun createNote(noteRequest: NoteRequest): NoteResponse {
        val note = noteRepository.save(Note(noteRequest, tokenService.getCurrentUserId()))
        return NoteResponse(note)
    }

    @PreAuthorize("hasRole('USER')")
    fun getNote(id: NoteId): NoteResponse {
        val note = noteRepository.getById(id)
        checkIfUserIdIsCurrentUser(note.createdBy)
        return NoteResponse(note)
    }

    @PreAuthorize("hasRole('USER')")
    fun updateNote(
        id: NoteId,
        noteRequest: NoteRequest,
    ): NoteResponse {
        val note = noteRepository.getById(id)
        checkIfUserIdIsCurrentUser(note.createdBy)
        note.update(noteRequest)
        return NoteResponse(note)
    }

    @PreAuthorize("hasRole('USER')")
    fun deleteNote(id: NoteId) {
        val note = noteRepository.getById(id)
        checkIfUserIdIsCurrentUser(note.createdBy)
        noteRepository.delete(note)
    }

    private fun checkIfUserIdIsCurrentUser(userId: UserId) {
        if (tokenService.getCurrentUserId() != userId) {
            throw DomainEntityNotFoundException("Note not found")
        }
    }
}
