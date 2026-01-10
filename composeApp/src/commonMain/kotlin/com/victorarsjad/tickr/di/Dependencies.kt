package com.victorarsjad.tickr.di

import com.victorarsjad.tickr.data.InMemorySessionRepository
import com.victorarsjad.tickr.data.InMemoryTickrRepository
import com.victorarsjad.tickr.domain.repository.SessionRepository
import com.victorarsjad.tickr.domain.repository.TickrRepository
import com.victorarsjad.tickr.domain.usecase.CreateTickrUseCase
import com.victorarsjad.tickr.domain.usecase.GetReportDataUseCase
import com.victorarsjad.tickr.domain.usecase.IncrementCountUseCase
import com.victorarsjad.tickr.domain.usecase.StartSessionUseCase
import com.victorarsjad.tickr.domain.usecase.StopSessionUseCase
import com.victorarsjad.tickr.util.UUIDGenerator

// TODO: Migrate to Koin modules for all targets. Android already uses Koin; keep this for non-Android targets.
object Dependencies {
    private val uuidGenerator by lazy { UUIDGenerator() }

    private val tickrRepositoryInternal: TickrRepository by lazy {
        InMemoryTickrRepository(uuidGenerator)
    }

    private val sessionRepositoryInternal: SessionRepository by lazy {
        InMemorySessionRepository()
    }

    val tickrRepository: TickrRepository get() = tickrRepositoryInternal
    val sessionRepository: SessionRepository get() = sessionRepositoryInternal

    val createTickrUseCase: CreateTickrUseCase by lazy {
        CreateTickrUseCase(tickrRepositoryInternal, uuidGenerator)
    }

    val incrementCountUseCase: IncrementCountUseCase by lazy {
        IncrementCountUseCase(tickrRepositoryInternal)
    }

    val startSessionUseCase: StartSessionUseCase by lazy {
        StartSessionUseCase(sessionRepositoryInternal, uuidGenerator)
    }

    val stopSessionUseCase: StopSessionUseCase by lazy {
        StopSessionUseCase(sessionRepositoryInternal)
    }

    val getReportDataUseCase: GetReportDataUseCase by lazy {
        GetReportDataUseCase(tickrRepositoryInternal, sessionRepositoryInternal)
    }
}
