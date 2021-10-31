package com.rankchoice.vote.service

import com.rankchoice.vote.TestUtilities
import com.rankchoice.vote.entity.Poll
import com.rankchoice.vote.entity.PollOption
import com.rankchoice.vote.entity.PollRequest
import com.rankchoice.vote.entity.PollStatus
import com.rankchoice.vote.repository.PollRepository
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PollServiceTest {
    private val pollRepo = mockk<PollRepository>()
    private val pollService = PollService(
        pollRepo
    )

    @Test
    fun findPollSuccess() {
        val id = UUID.randomUUID()
        val expectedPoll = Poll(
            id = id,
            name = "name",
            description = "description",
            ownerId = UUID.randomUUID(),
            status = PollStatus.Scheduled,
            startTime = Instant.now(),
            endTime = Instant.now(),
            options = listOf(
                PollOption("opt1", "desc1"),
                PollOption("opt2", "desc2")
            )
        )
        every { pollRepo.findByIdOrNull(id) } returns expectedPoll

        val result = pollService.findPoll(id)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(id, result.body!!.id)
        assertEquals(expectedPoll, result.body)
    }

    @Test
    fun findPollNotFound() {
        val id = UUID.randomUUID()
        every { pollRepo.findByIdOrNull(id) } returns null

        val result = pollService.findPoll(id)

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @Test
    fun findPollIllegalArgument() {
        val id = UUID.randomUUID()
        every { pollRepo.findByIdOrNull(id) } throws IllegalArgumentException("test iae")

        val result = pollService.findPoll(id)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @Test
    fun findPollDataInaccessible() {
        val id = UUID.randomUUID()
        every { pollRepo.findByIdOrNull(id) } throws DataRetrievalFailureException("test dae")

        val result = pollService.findPoll(id)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
    }

    @Test
    fun createPollSuccess() {
        val ownerId = UUID.randomUUID()
        val id = UUID.randomUUID()
        val startTime = Instant.now()
        val endTime = Instant.now().plus(5, ChronoUnit.DAYS)
        val request = PollRequest(
            name = "test poll",
            description = "description for test poll",
            ownerId = ownerId,
            startTime = startTime,
            endTime = endTime,
            options = listOf(
                PollOption("opt1", "desc1"),
                PollOption("opt2", "desc2")
            )
        )
        val expectedPoll = Poll(
            id = id,
            name = "test poll",
            description = "description for test poll",
            ownerId = ownerId,
            status = PollStatus.Scheduled,
            startTime = startTime,
            endTime = endTime,
            options = request.options
        )
        every { pollRepo.save<Poll>(any()) } returns expectedPoll

        val result = pollService.createPoll(request)

        verify {
            pollRepo.save<Poll>(withArg {
                TestUtilities.validateIdReplaced(expectedPoll, it, "id")
            })
        }
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(id.toString(), result.body)
    }

    @Test
    fun createPollBadRequestOnSave() {
        val request = PollRequest(
            name = "test poll",
            description = "description for test poll",
            ownerId = UUID.randomUUID(),
            startTime = Instant.now(),
            endTime = Instant.now().plus(5, ChronoUnit.DAYS),
            options = listOf(
                PollOption("opt1", "desc1"),
                PollOption("opt2", "desc2")
            )
        )
        every { pollRepo.save<Poll>(any()) } throws IllegalArgumentException("test iae")

        val result = pollService.createPoll(request)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertTrue(result.body.isNullOrEmpty())
    }

    @Test
    fun createPollDataInaccessibleOnSave() {
        val request = PollRequest(
            name = "test poll",
            description = "description for test poll",
            ownerId = UUID.randomUUID(),
            startTime = Instant.now(),
            endTime = Instant.now().plus(5, ChronoUnit.DAYS),
            options = listOf(
                PollOption("opt1", "desc1"),
                PollOption("opt2", "desc2")
            )
        )
        every { pollRepo.save<Poll>(any()) } throws DataAccessResourceFailureException("test dae")

        val result = pollService.createPoll(request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
        assertTrue(result.body.isNullOrEmpty())
    }

    @Test
    fun createPollValidateEndDate() {
        val request = PollRequest(
            name = "test poll",
            description = "description for test poll",
            ownerId = UUID.randomUUID(),
            startTime = null,
            endTime = Instant.now(),
            options = listOf(
                PollOption("opt1", "desc1"),
                PollOption("opt2", "desc2")
            )
        )

        val result = pollService.createPoll(request)

        verify { pollRepo wasNot Called }
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertTrue(result.body.isNullOrEmpty())
        // TODO: useful validation messages
    }

    @Test
    fun updatePollSuccess() {
        val ownerId = UUID.randomUUID()
        val ownerIdNew = UUID.randomUUID()
        val id = UUID.randomUUID()
        val startTime = Instant.now()
        val endTime = Instant.now().plus(4, ChronoUnit.DAYS)
        val endTimeNew = endTime.plus(1, ChronoUnit.DAYS)
        val request = PollRequest(
            name = "new test poll",
            description = "new description for test poll",
            ownerId = ownerIdNew,
            startTime = null,
            endTime = endTimeNew,
            options = listOf(
                PollOption("new opt1", "desc1"),
                PollOption("new opt2", "desc2")
            )
        )
        val existingPoll = Poll(
            id = id,
            name = "test poll",
            description = "description for test poll",
            ownerId = ownerId,
            status = PollStatus.Scheduled,
            startTime = startTime,
            endTime = endTime,
            options = emptyList()
        )
        val expectedPoll = Poll(
            id = id,
            name = "new test poll", // name gets updated
            description = "new description for test poll", // description gets updated
            ownerId = ownerId, // ownerId does not get updated
            status = PollStatus.Scheduled, // status is still scheduled
            startTime = startTime, // startTime uses existing, because request has null startTime
            endTime = endTimeNew, // endTime gets updated
            options = request.options // options get updated
        )
        every { pollRepo.findByIdOrNull(id) } returns existingPoll
        every { pollRepo.save(expectedPoll) } returns expectedPoll

        val result = pollService.updatePoll(request, id)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(id.toString(), result.body)
    }

    @Test
    fun updatePollNotFound() {
        val id = UUID.randomUUID()
        val request = PollRequest(
            name = "new test poll",
            description = "new description for test poll",
            ownerId = UUID.randomUUID(),
            startTime = Instant.now(),
            endTime = Instant.now().plus(5, ChronoUnit.DAYS),
            options = listOf(
                PollOption("new opt1", "desc1"),
                PollOption("new opt2", "desc2")
            )
        )
        every { pollRepo.findByIdOrNull(id) } returns null

        val result = pollService.updatePoll(request, id)

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
        assertTrue(result.body.isNullOrEmpty())
    }

    @Test
    fun updatePollBadRequest() {
        val id = UUID.randomUUID()
        val request = PollRequest(
            name = "new test poll",
            description = "new description for test poll",
            ownerId = UUID.randomUUID(),
            startTime = Instant.now(),
            endTime = Instant.now().plus(5, ChronoUnit.DAYS),
            options = listOf(
                PollOption("new opt1", "desc1"),
                PollOption("new opt2", "desc2")
            )
        )
        every { pollRepo.findByIdOrNull(id) } throws IllegalArgumentException("test iae")

        val result = pollService.updatePoll(request, id)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertTrue(result.body.isNullOrEmpty())
    }

    @Test
    fun updatePollDataInaccessible() {
        val id = UUID.randomUUID()
        val request = PollRequest(
            name = "new test poll",
            description = "new description for test poll",
            ownerId = UUID.randomUUID(),
            startTime = Instant.now(),
            endTime = Instant.now().plus(5, ChronoUnit.DAYS),
            options = listOf(
                PollOption("new opt1", "desc1"),
                PollOption("new opt2", "desc2")
            )
        )
        every { pollRepo.findByIdOrNull(id) } throws DataRetrievalFailureException("test dae")

        val result = pollService.updatePoll(request, id)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
        assertTrue(result.body.isNullOrEmpty())
    }

    @Test
    fun updatePollPastScheduled() {
        val ownerId = UUID.randomUUID()
        val ownerIdNew = UUID.randomUUID()
        val id = UUID.randomUUID()
        val startTime = Instant.now()
        val endTime = Instant.now().plus(4, ChronoUnit.DAYS)
        val endTimeNew = endTime.plus(1, ChronoUnit.DAYS)
        val request = PollRequest(
            name = "new test poll",
            description = "new description for test poll",
            ownerId = ownerIdNew,
            startTime = null,
            endTime = endTimeNew,
            options = listOf(
                PollOption("new opt1", "desc1"),
                PollOption("new opt2", "desc2")
            )
        )
        val existingPoll = Poll(
            id = id,
            name = "test poll",
            description = "description for test poll",
            ownerId = ownerId,
            status = PollStatus.Active,
            startTime = startTime,
            endTime = endTime,
            options = emptyList()
        )
        every { pollRepo.findByIdOrNull(id) } returns existingPoll

        val result = pollService.updatePoll(request, id)

        assertEquals(HttpStatus.FORBIDDEN, result.statusCode)
        assertTrue(result.body!!.isNotBlank())
    }

    @Test
    fun updatePollInvalidName() {
        val ownerId = UUID.randomUUID()
        val ownerIdNew = UUID.randomUUID()
        val id = UUID.randomUUID()
        val startTime = Instant.now()
        val endTime = Instant.now().plus(4, ChronoUnit.DAYS)
        val endTimeNew = endTime.plus(1, ChronoUnit.DAYS)
        val request = PollRequest(
            name = "", // name cannot be empty
            description = "new description for test poll",
            ownerId = ownerIdNew,
            startTime = null,
            endTime = endTimeNew,
            options = listOf(
                PollOption("new opt1", "desc1"),
                PollOption("new opt2", "desc2")
            )
        )
        val existingPoll = Poll(
            id = id,
            name = "test poll",
            description = "description for test poll",
            ownerId = ownerId,
            status = PollStatus.Scheduled,
            startTime = startTime,
            endTime = endTime,
            options = emptyList()
        )
        every { pollRepo.findByIdOrNull(id) } returns existingPoll

        val result = pollService.updatePoll(request, id)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertTrue(result.body.isNullOrBlank())
    }
}
