package me.bitnet.secretstash.note.service

import io.github.oshai.kotlinlogging.KotlinLogging
import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.domain.NoteId
import me.bitnet.secretstash.note.job.NoteExpirationJob
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZonedDateTime
import java.util.Date

@Service
class NoteExpirationService(
    private val scheduler: Scheduler,
) {
    private val logger = KotlinLogging.logger {}

    fun scheduleNoteDeletion(note: Note) {
        // If no expiration is set, cancel any existing job
        if (note.expiresAt == null) {
            cancelScheduledDeletion(note.id)
            return
        }

        try {
            val now = ZonedDateTime.now()
            val expiresAt = note.expiresAt

            if (expiresAt?.isBefore(now) == true) {
                logger.warn { "[scheduleNoteDeletion] Expires in the past: ${note.id}. Scheduling immediate deletion." }
                scheduleImmediateDeletion(note.id)
                return
            }

            val delay = Duration.between(now, expiresAt).toMillis()
            val jobDataMap =
                JobDataMap().apply {
                    put(NoteExpirationJob.NOTE_ID_KEY, note.id.toString())
                }

            val jobKey = getJobKey(note.id)
            val jobDetail =
                JobBuilder
                    .newJob(NoteExpirationJob::class.java)
                    .withIdentity(jobKey)
                    .usingJobData(jobDataMap)
                    .storeDurably(false)
                    .requestRecovery(true)
                    .build()

            val triggerKey = "trigger-${note.id}"
            val trigger =
                TriggerBuilder
                    .newTrigger()
                    .withIdentity(triggerKey, "note-expiration-triggers")
                    .startAt(Date(System.currentTimeMillis() + delay))
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withMisfireHandlingInstructionFireNow(),
                    ).forJob(jobKey)
                    .build()

            val existingJob = scheduler.checkExists(jobKey)

            if (existingJob) {
                logger.info { "[scheduleNoteDeletion] Updating existing job for note: ${note.id}" }
                scheduler.deleteJob(jobKey)
                scheduler.scheduleJob(jobDetail, trigger)
            } else {
                logger.info { "[scheduleNoteDeletion] Scheduling new job for note: ${note.id}, expires at: $expiresAt" }
                scheduler.scheduleJob(jobDetail, trigger)
            }
        } catch (e: Exception) {
            logger.error(e) { "[scheduleNoteDeletion] Failed to schedule note deletion for note: ${note.id}" }
            throw e
        }
    }

    fun cancelScheduledDeletion(noteId: NoteId) {
        try {
            val jobKey = getJobKey(noteId)
            val exists = scheduler.checkExists(jobKey)

            if (exists) {
                logger.info { "[cancelScheduledDeletion] Cancelling scheduled deletion for note: $noteId" }
                scheduler.deleteJob(jobKey)
            }
        } catch (e: Exception) {
            logger.error(e) { "[cancelScheduledDeletion] Failed to cancel scheduled deletion for note: $noteId" }
            throw e
        }
    }

    private fun scheduleImmediateDeletion(noteId: NoteId) {
        try {
            val jobDataMap =
                JobDataMap().apply {
                    put(NoteExpirationJob.NOTE_ID_KEY, noteId.toString())
                }

            val jobKey = getJobKey(noteId)
            val jobDetail =
                JobBuilder
                    .newJob(NoteExpirationJob::class.java)
                    .withIdentity(jobKey)
                    .usingJobData(jobDataMap)
                    .storeDurably(false)
                    .requestRecovery(true)
                    .build()

            val triggerKey = "trigger-immediate-$noteId"
            val trigger =
                TriggerBuilder
                    .newTrigger()
                    .withIdentity(triggerKey, "note-expiration-triggers")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                    .forJob(jobKey)
                    .build()

            scheduler.scheduleJob(jobDetail, trigger)

            logger.info { "[scheduleImmediateDeletion] Scheduled immediate deletion for note: $noteId" }
        } catch (e: Exception) {
            logger.error(e) { "[scheduleImmediateDeletion] Failed to schedule immediate deletion for note: $noteId" }
            throw e
        }
    }

    private fun getJobKey(noteId: NoteId): JobKey = JobKey.jobKey("note-expiration-$noteId", "note-expiration-jobs")
}
