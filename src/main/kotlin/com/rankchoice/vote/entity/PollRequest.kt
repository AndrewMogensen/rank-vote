package com.rankchoice.vote.entity

import java.time.Instant
import java.util.UUID

data class PollRequest(
    val name: String,
    val description: String,
    val ownerId: UUID,
    val startTime: Instant?,
    val endTime: Instant,
    val options: List<PollOption>
)
