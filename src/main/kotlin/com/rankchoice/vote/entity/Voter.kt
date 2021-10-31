package com.rankchoice.vote.entity

import org.springframework.data.annotation.Id
import java.util.UUID

data class Voter(
    @Id val id: UUID,
    val pollId: UUID,
    val selections: List<VoterSelection>
)