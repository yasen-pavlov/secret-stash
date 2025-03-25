package me.bitnet.secretstash.note.service

import me.bitnet.secretstash.note.domain.Note
import me.bitnet.secretstash.note.job.NoteExpirationJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.Trigger
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NoteExpirationServiceTest {
    @Mock
    private lateinit var scheduler: Scheduler

    @InjectMocks
    private lateinit var noteExpirationService: NoteExpirationService

    @Captor
    private lateinit var jobDetailCaptor: ArgumentCaptor<JobDetail>

    @Captor
    private lateinit var triggerCaptor: ArgumentCaptor<Trigger>

    @Captor
    private lateinit var jobKeyCaptor: ArgumentCaptor<JobKey>

    private lateinit var note: Note
    private val noteId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        note =
            Note(
                id = noteId,
                title = "Test Note",
                content = "Test Content",
                createdBy = userId,
            )
    }

    @Test
    fun `should not schedule deletion when note has no expiration`() {
        // Arrange
        note.expiresAt = null

        // Act
        noteExpirationService.scheduleNoteDeletion(note)

        // Assert
        verify(scheduler, never()).scheduleJob(Mockito.any(JobDetail::class.java), Mockito.any(Trigger::class.java))
    }

    @Test
    fun `should schedule deletion when note has future expiration timestamp in the future`() {
        // Arrange
        note.expiresAt = ZonedDateTime.now(UTC).plusMinutes(5)
        whenever(scheduler.checkExists(Mockito.any(JobKey::class.java))).thenReturn(false)

        // Act
        noteExpirationService.scheduleNoteDeletion(note)

        // Assert
        verify(scheduler).scheduleJob(jobDetailCaptor.capture(), triggerCaptor.capture())

        val jobDetail = jobDetailCaptor.value

        assertThat(jobDetail.key.name).isEqualTo("note-expiration-$noteId")
        assertThat(jobDetail.key.group).isEqualTo("note-expiration-jobs")
        assertThat(jobDetail.jobClass).isEqualTo(NoteExpirationJob::class.java)

        val jobDataMap = jobDetail.jobDataMap
        assertThat(jobDataMap.getString(NoteExpirationJob.NOTE_ID_KEY)).isEqualTo(noteId.toString())
    }

    @Test
    fun `should schedule immediate deletion when note has past expiration time`() {
        // Arrange
        note.expiresAt = ZonedDateTime.now(UTC).minusMinutes(5)

        // Act
        noteExpirationService.scheduleNoteDeletion(note)

        // Assert
        verify(scheduler).scheduleJob(jobDetailCaptor.capture(), triggerCaptor.capture())

        val trigger = triggerCaptor.value
        assertThat(trigger.key.name).startsWith("trigger-immediate-")

        assertThat(trigger.startTime.time).isLessThanOrEqualTo(System.currentTimeMillis() + 1000)
    }

    @Test
    fun `should update existing job when job already exists`() {
        // Arrange
        note.expiresAt = ZonedDateTime.now(UTC).plusMinutes(10)
        whenever(scheduler.checkExists(Mockito.any(JobKey::class.java))).thenReturn(true)

        // Act
        noteExpirationService.scheduleNoteDeletion(note)

        // Assert
        verify(scheduler).deleteJob(Mockito.any(JobKey::class.java))
        verify(scheduler).scheduleJob(Mockito.any(JobDetail::class.java), Mockito.any(Trigger::class.java))
    }

    @Test
    fun `should cancel scheduled deletion when cancelScheduledDeletion is called`() {
        // Arrange
        whenever(scheduler.checkExists(Mockito.any(JobKey::class.java))).thenReturn(true)

        // Act
        noteExpirationService.cancelScheduledDeletion(noteId)

        // Assert
        verify(scheduler).deleteJob(jobKeyCaptor.capture())

        val jobKey = jobKeyCaptor.value
        assertThat(jobKey.name).isEqualTo("note-expiration-$noteId")
        assertThat(jobKey.group).isEqualTo("note-expiration-jobs")
    }

    @Test
    fun `should not delete job when job doesn't exist`() {
        // Arrange
        whenever(scheduler.checkExists(Mockito.any(JobKey::class.java))).thenReturn(false)

        // Act
        noteExpirationService.cancelScheduledDeletion(noteId)

        // Assert
        verify(scheduler, never()).deleteJob(Mockito.any(JobKey::class.java))
    }
}
