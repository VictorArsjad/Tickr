package com.victorarsjad.tickr.domain.usecase

import com.victorarsjad.tickr.domain.model.Period
import com.victorarsjad.tickr.domain.model.Tickr
import com.victorarsjad.tickr.domain.model.TickrType
import com.victorarsjad.tickr.domain.repository.TickrRepository
import com.victorarsjad.tickr.util.TimeUtils.Companion.getTimeMillis
import com.victorarsjad.tickr.util.UUIDGenerator


class CreateTickrUseCase(
    private val tickrRepository: TickrRepository,
    private val uuidGenerator: UUIDGenerator
) {
    suspend operator fun invoke(name: String, type: TickrType, period: Period): Tickr {
        require(name.length in 1..50) { "Name must be 1-50 characters" }
        val now = getTimeMillis()
        val newTickr = Tickr(
            id = uuidGenerator.generate(),
            name = name,
            type = type,
            period = period,
            createdAt = now,
            updatedAt = now
        )
        return tickrRepository.createTickr(newTickr)
    }
}
