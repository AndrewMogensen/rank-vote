package com.rankchoice.vote.repository

import com.rankchoice.vote.entity.Voter
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.UUID

interface VoterRepository : MongoRepository<Voter, UUID>