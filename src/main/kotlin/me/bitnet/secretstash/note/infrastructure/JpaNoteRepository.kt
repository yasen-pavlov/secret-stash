package me.bitnet.secretstash.note.infrastructure

import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import org.springframework.data.jpa.repository.JpaRepository

interface JpaNoteRepository : JpaRepository<Note, NoteId>
