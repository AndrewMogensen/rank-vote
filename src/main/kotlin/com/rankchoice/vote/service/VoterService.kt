package com.rankchoice.vote.service

import com.rankchoice.vote.entity.Voter
import com.rankchoice.vote.entity.VoterSelection
import com.rankchoice.vote.repository.VoterRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.util.UUID

@Service
class VoterService @Autowired constructor(
    private val voterRepository: VoterRepository
){
    private val log = KotlinLogging.logger {}

    fun findVoter(id: UUID): ResponseEntity<Voter> {
        return try {
            val voter = voterRepository.findByIdOrNull(id)
            voter?.let {
                ResponseEntity(it, HttpStatus.OK)
            } ?: ResponseEntity(HttpStatus.NOT_FOUND)

        } catch (dae: DataAccessException) {
            log.error(dae) { "Failed to access Voter '$id'" }
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (iae: IllegalArgumentException) {
            log.error(iae) { "Bad request finding Voter '$id'" }
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }

    fun createVoter(pollId: UUID): ResponseEntity<String> {
        return try {
            val voterId = UUID.randomUUID()
            val voter = Voter(
                voterId,
                pollId,
                emptyList()
            )
            val savedVoter = voterRepository.save(voter)
            ResponseEntity(savedVoter.id.toString(), HttpStatus.CREATED)

        } catch (dae: DataAccessException) {
            log.error(dae) { "Failed to save Voter for poll '$pollId'" }
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (iae: IllegalArgumentException) {
            log.error(iae) { "Bad request to save Voter for poll '$pollId'" }
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }

    fun updateSelections(voterId: UUID, selections: List<VoterSelection>): ResponseEntity<String> {
        val selectionErrors = validateSelections(selections)
        if (selectionErrors.isNotEmpty()) {
            return ResponseEntity(selectionErrors, HttpStatus.BAD_REQUEST)
        }
        return try {
            val existingVoter = voterRepository.findByIdOrNull(voterId)
            existingVoter?.let {
                val updatedVoter = Voter(
                    id = voterId,
                    pollId = existingVoter.pollId,
                    selections = selections
                )
                val savedVoter: Voter = voterRepository.save(updatedVoter)
                ResponseEntity(savedVoter.id.toString(), HttpStatus.OK)
            } ?: ResponseEntity(HttpStatus.NOT_FOUND)

        } catch (dae: DataAccessException) {
            log.error(dae) { "Failed to access Voter '$voterId'" }
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (iae: IllegalArgumentException) {
            log.error(iae) { "Bad request for Voter '$voterId'" }
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }

    private fun validateSelections(selections: List<VoterSelection>): String {
        var error = ""
        val rankTracker = IntArray(selections.size)
        selections.forEach {
            val currentRank = it.rank
            if (currentRank > selections.size || currentRank <= 0) {
                error += "Option '${it.optionName}' rank outside range. "
            }
            else if (rankTracker[currentRank - 1] == 1) {
                error += "Option '${it.optionName}' repeated rank. "
            }
            rankTracker[currentRank - 1] = 1
        }
        return error
    }
}