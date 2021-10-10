package com.rankchoice.vote.entity

import org.springframework.data.annotation.Id
import java.time.Instant
import java.util.UUID

data class Poll(
    @Id
    val id: UUID,
    val name: String,
    val description: String,
    val ownerId: UUID,
    val status: PollStatus,
    val startTime: Instant,
    val endTime: Instant,
    val options: List<PollOption>
)