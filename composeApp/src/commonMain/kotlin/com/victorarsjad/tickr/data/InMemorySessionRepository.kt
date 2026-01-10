package com.victorarsjad.tickr.data

// TODO: Replace with SQLDelight-backed implementation per platform. Android uses SqlDelightSessionRepository.

import com.victorarsjad.tickr.domain.model.TimeSession
import com.victorarsjad.tickr.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class InMemorySessionRepository : SessionRepository {
    private val mutex = Mutex()
    private val sessions = mutableListOf<TimeSession>()
    private val sessionsFlow = MutableStateFlow<List<TimeSession>>(emptyList())

    override fun getSessionsForTickr(tickrId: String): Flow<List<TimeSession>> =
        sessionsFlow.map { list -> list.filter { it.tickrId == tickrId } }

    override fun getActiveSessionForTickr(tickrId: String): Flow<TimeSession?> =
        sessionsFlow.map { list -> list.firstOrNull { it.tickrId == tickrId && it.endedAt == null } }

    override fun getSessionsForPeriod(tickrId: String, startTime: Long, endTime: Long): List<TimeSession> =
        sessions.filter { it.tickrId == tickrId && it.startedAt >= startTime && it.startedAt < endTime }

    override suspend fun createSession(session: TimeSession): TimeSession = mutex.withLock {
        sessions.add(session)
        sessionsFlow.value = sessions.toList()
        session
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun stopSession(sessionId: String) = mutex.withLock {
        val now = Clock.System.now().toEpochMilliseconds()
        val idx = sessions.indexOfFirst { it.id == sessionId }
        if (idx >= 0) {
            val s = sessions[idx]
            if (s.endedAt == null) {
                sessions[idx] = s.copy(endedAt = now, updatedAt = now)
                sessionsFlow.value = sessions.toList()
            }
        }
    }

    override suspend fun deleteSession(sessionId: String) = mutex.withLock {
        val idx = sessions.indexOfFirst { it.id == sessionId }
        if (idx >= 0) {
            sessions.removeAt(idx)
            sessionsFlow.value = sessions.toList()
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getTotalDurationForPeriod(tickrId: String, startTime: Long, endTime: Long): Long =
        getSessionsForPeriod(tickrId, startTime, endTime).sumOf { (it.endedAt ?: Clock.System.now().toEpochMilliseconds()) - it.startedAt }

    override fun getAllActiveSessions(): Flow<List<TimeSession>> =
        sessionsFlow.map { list -> list.filter { it.endedAt == null } }

    override fun getActiveSessions(): Flow<List<TimeSession>> =
        sessionsFlow.map { list -> list.filter { it.endedAt == null } }
}
