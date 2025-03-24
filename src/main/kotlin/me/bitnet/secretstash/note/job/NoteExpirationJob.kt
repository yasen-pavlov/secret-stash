package me.bitnet.secretstash.note.job

import io.github.oshai.kotlinlogging.KotlinLogging
import me.bitnet.secretstash.note.infrastructure.NoteRepository
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.quartz.PersistJobDataAfterExecution
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
class NoteExpirationJob(
    private val noteRepository: NoteRepository,
) : QuartzJobBean() {
    private val logger = KotlinLogging.logger {}

    companion object {
        const val NOTE_ID_KEY = "noteId"
    }

    @Transactional
    override fun executeInternal(context: JobExecutionContext) {
        val jobDataMap = context.mergedJobDataMap
        val noteIdString = jobDataMap.getString(NOTE_ID_KEY)

        if (noteIdString != null) {
            try {
                val noteId = UUID.fromString(noteIdString)
                logger.info { "[NoteExpirationJob] Processing expiration for note: $noteId" }

                try {
                    val note = noteRepository.getById(noteId)
                    logger.info { "[NoteExpirationJob] Deleting expired note: $noteId" }
                    noteRepository.delete(note)
                    logger.info { "[NoteExpirationJob] Successfully deleted note: $noteId" }
                } catch (e: Exception) {
                    // If note not found, it might have been already deleted
                    logger.warn { "[NoteExpirationJob] Note not found or already deleted: $noteId" }
                }
            } catch (e: IllegalArgumentException) {
                logger.error { "[NoteExpirationJob] Invalid note ID format: $noteIdString" }
            } catch (e: Exception) {
                logger.error(e) { "[NoteExpirationJob] Error deleting expired note: $noteIdString" }
                throw e // Rethrowing to allow Quartz to handle retries
            }
        } else {
            logger.error { "[NoteExpirationJob] No noteId found in job data" }
        }
    }
}
