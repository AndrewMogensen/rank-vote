package com.rankchoice.vote.service

import com.rankchoice.vote.entity.Poll
import com.rankchoice.vote.entity.PollRequest
import com.rankchoice.vote.entity.PollStatus
import com.rankchoice.vote.repository.PollRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.time.Instant
import java.util.*
import mu.KotlinLogging

@Service
class PollService @Autowired constructor(
    private val pollRepository: PollRepository
){
    private val log = KotlinLogging.logger {}

    fun findPoll(id: UUID): ResponseEntity<Poll> {
        return try {
            val poll = pollRepository.findByIdOrNull(id)
            poll?.let {
                ResponseEntity(it, HttpStatus.OK)
            } ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (dae: DataAccessException) {
            log.error(dae) { "Failed to access Poll '$id'" }
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (iae: IllegalArgumentException) {
            log.error(iae) { "Bad request finding Poll '$id'" }
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }

    fun createPoll(pollRequest: PollRequest): ResponseEntity<String> {
        return try {
            val pollEntity = Poll(
                id = UUID.randomUUID(),
                name = pollRequest.name,
                description = pollRequest.description,
                ownerId = UUID.randomUUID(),
                status = PollStatus.Scheduled,
                startTime = pollRequest.startTime ?: Instant.now(),
                endTime = pollRequest.endTime,
                options = pollRequest.options
            )
            val savedPoll: Poll = pollRepository.save(pollEntity)
            ResponseEntity(savedPoll.id.toString(), HttpStatus.CREATED)
        } catch (dae: DataAccessException) {
            log.error(dae) { "Failed to save Poll '${pollRequest.name}'" }
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (iae: IllegalArgumentException) {
            log.error(iae) { "Bad request to save Poll '${pollRequest.name}'" }
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }
}