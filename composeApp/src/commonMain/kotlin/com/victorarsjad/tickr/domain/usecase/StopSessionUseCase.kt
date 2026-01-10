package com.victorarsjad.tickr.domain.usecase

import com.victorarsjad.tickr.domain.repository.SessionRepository

class StopSessionUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: String) {
        sessionRepository.stopSession(sessionId)
    }
}
