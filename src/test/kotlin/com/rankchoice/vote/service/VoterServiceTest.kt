package com.rankchoice.vote.service

import com.rankchoice.vote.TestUtilities
import com.rankchoice.vote.entity.Voter
import com.rankchoice.vote.entity.VoterSelection
import com.rankchoice.vote.repository.VoterRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class VoterServiceTest {
    private val voterRepo = mockk<VoterRepository>()
    private val voterService = VoterService(
        voterRepo
    )

    @Test
    fun findVoterSuccess() {
        val id = UUID.randomUUID()
        val expectedVoter = Voter(
            id = id,
            pollId = UUID.randomUUID(),
            selections = listOf(
                VoterSelection("opt1", 2),
                VoterSelection("opt2", 1)
            )
        )
        every { voterRepo.findByIdOrNull(id) } returns expectedVoter

        val result = voterService.findVoter(id)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(id, result.body!!.id)
        assertEquals(expectedVoter, result.body)
    }

    @Test
    fun findPollNotFound() {
        val id = UUID.randomUUID()
        every { voterRepo.findByIdOrNull(id) } returns null

        val result = voterService.findVoter(id)

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @Test
    fun findPollIllegalArgument() {
        val id = UUID.randomUUID()
        every { voterRepo.findByIdOrNull(id) } throws IllegalArgumentException("test iae")

        val result = voterService.findVoter(id)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @Test
    fun findPollDataInaccessible() {
        val id = UUID.randomUUID()
        every { voterRepo.findByIdOrNull(id) } throws DataRetrievalFailureException("test dae")

        val result = voterService.findVoter(id)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
    }

    @Test
    fun createVoterSuccess() {
        val id = UUID.randomUUID()
        val pollId = UUID.randomUUID()
        val expectedVoter = Voter(
            id = id,
            pollId = pollId,
            selections = emptyList()
        )
        every { voterRepo.save<Voter>(any()) } returns expectedVoter

        val result = voterService.createVoter(pollId)

        verify {
            voterRepo.save<Voter>(withArg {
                TestUtilities.validateIdReplaced(expectedVoter, it, "id")
            })
        }
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(id.toString(), result.body)
    }

    @Test
    fun createVoterBadRequestOnSave() {
        val pollId = UUID.randomUUID()
        every { voterRepo.save<Voter>(any()) } throws IllegalArgumentException("test iae")

        val result = voterService.createVoter(pollId)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertTrue(result.body.isNullOrEmpty())
    }


    @Test
    fun createVoterDataInaccessibleOnSave() {
        val pollId = UUID.randomUUID()
        every { voterRepo.save<Voter>(any()) } throws DataAccessResourceFailureException("test iae")

        val result = voterService.createVoter(pollId)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
        assertTrue(result.body.isNullOrEmpty())
    }

    @Test
    fun updateSelectionsSuccess() {
        val voterId = UUID.randomUUID()
        val pollId = UUID.randomUUID()
        val selections = listOf(
            VoterSelection("opt1", 2),
            VoterSelection("opt2", 3),
            VoterSelection("opt3", 1)
        )
        val expectedFindVoter = Voter(
            voterId, pollId, emptyList()
        )
        every { voterRepo.findByIdOrNull(voterId) } returns expectedFindVoter
        val expectedSavedVoter = Voter(
            voterId, pollId, selections
        )
        every { voterRepo.save(expectedSavedVoter) } returns expectedSavedVoter

        val result = voterService.updateSelections(voterId, selections)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(voterId.toString(), result.body)
    }

    @Test
    fun updateSelectionsNotFound() {
        val voterId = UUID.randomUUID()
        val selections = listOf(
            VoterSelection("opt1", 2),
            VoterSelection("opt2", 3),
            VoterSelection("opt3", 1)
        )
        every { voterRepo.findByIdOrNull(voterId) } returns null

        val result = voterService.updateSelections(voterId, selections)

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
        assertNull(result.body)
    }

    @Test
    fun updateSelectionsBadRequest() {
        val voterId = UUID.randomUUID()
        val selections = listOf(
            VoterSelection("opt1", 2),
            VoterSelection("opt2", 3),
            VoterSelection("opt3", 1)
        )
        every { voterRepo.findByIdOrNull(voterId) } throws IllegalArgumentException("test iae")

        val result = voterService.updateSelections(voterId, selections)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertNull(result.body)
    }

    @Test
    fun updateSelectionsDataInaccessible() {
        val voterId = UUID.randomUUID()
        val selections = listOf(
            VoterSelection("opt1", 2),
            VoterSelection("opt2", 3),
            VoterSelection("opt3", 1)
        )
        every { voterRepo.findByIdOrNull(voterId) } throws DataRetrievalFailureException("test iae")

        val result = voterService.updateSelections(voterId, selections)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
        assertNull(result.body)
    }

    // TODO: validation error exceptions
}