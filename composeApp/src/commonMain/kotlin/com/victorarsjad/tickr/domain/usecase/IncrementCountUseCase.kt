package com.victorarsjad.tickr.domain.usecase

import com.victorarsjad.tickr.domain.repository.TickrRepository

class IncrementCountUseCase(
    private val tickrRepository: TickrRepository
) {
    suspend operator fun invoke(tickrId: String) {
        tickrRepository.incrementCount(tickrId)
    }
}
