package com.victorarsjad.tickr.domain.usecase

import com.victorarsjad.tickr.domain.model.TimeSession
import com.victorarsjad.tickr.domain.repository.SessionRepository
import com.victorarsjad.tickr.util.TimeUtils.Companion.getTimeMillis
import com.victorarsjad.tickr.util.UUIDGenerator

class StartSessionUseCase(
    private val sessionRepository: SessionRepository,
    private val uuidGenerator: UUIDGenerator
) {
    suspend operator fun invoke(tickrId: String): TimeSession {
        val now = getTimeMillis()
        val session = TimeSession(
            id = uuidGenerator.generate(),
            tickrId = tickrId,
            startedAt = now,
            endedAt = null,
            createdAt = now,
            updatedAt = now
        )
        return sessionRepository.createSession(session)
    }
}
