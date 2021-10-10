package com.rankchoice.vote.service

import com.rankchoice.vote.entity.Poll
import com.rankchoice.vote.entity.PollOption
import com.rankchoice.vote.entity.PollRequest
import com.rankchoice.vote.entity.PollStatus
import com.rankchoice.vote.repository.PollRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.http.HttpStatus
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.assertEquals

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
        every { pollRepo.findById(id) } returns Optional.of(expectedPoll)

        val result = pollService.findPoll(id)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(id, result.body!!.id)
        assertEquals(expectedPoll, result.body)
    }

    @Test
    fun findPollNotFound() {
        val id = UUID.randomUUID()
        every { pollRepo.findById(id) } returns Optional.empty()

        val result = pollService.findPoll(id)

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @Test
    fun findPollIllegalArgument() {
        val id = UUID.randomUUID()
        every { pollRepo.findById(id) } throws IllegalArgumentException("test iae")

        val result = pollService.findPoll(id)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @Test
    fun findPollDataInaccessible() {
        val id = UUID.randomUUID()
        every { pollRepo.findById(id) } throws DataRetrievalFailureException("test dae")

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
//        TODO: Figure out complex mock matching
//        every { pollRepo.save(any()) } returns expectedPoll
//
//        val result = pollService.createPoll(request)
//
//        assertEquals(HttpStatus.OK, result.statusCode)
//        assertEquals(expectedPoll.id, result.body)
    }

    @Test
    fun createPollBadRequest() {
        // TODO
    }

    @Test
    fun createPollDataInaccessible() {
        // TODO
    }
}