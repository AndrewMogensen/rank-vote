package com.rankchoice.vote.repository

import com.rankchoice.vote.entity.Poll
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface PollRepository : MongoRepository<Poll, UUID>
