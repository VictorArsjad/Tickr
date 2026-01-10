package com.victorarsjad.tickr.data.sql

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.victorarsjad.tickr.db.TickrDatabase
import com.victorarsjad.tickr.domain.model.TimeSession as DomainSession
import com.victorarsjad.tickr.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.System.currentTimeMillis

class SqlDelightSessionRepository(
    private val db: TickrDatabase
) : SessionRepository {
    override fun getSessionsForTickr(tickrId: String): Flow<List<DomainSession>> =
        db.tickrQueries.getSessionsForTickr(tickrId).asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toDomain() }
        }

    override fun getActiveSessionForTickr(tickrId: String): Flow<DomainSession?> =
        db.tickrQueries.getActiveSessionForTickr(tickrId).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() }

    override fun getSessionsForPeriod(tickrId: String, startTime: Long, endTime: Long): List<DomainSession> =
        db.tickrQueries.getSessionsForPeriod(tickrId, startTime, endTime).executeAsList().map { it.toDomain() }

    override suspend fun createSession(session: DomainSession): DomainSession {
        db.tickrQueries.insertSession(
            id = session.id,
            tickrId = session.tickrId,
            startedAt = session.startedAt,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt
        )
        return session
    }

    override suspend fun stopSession(sessionId: String) {
        val now = currentTimeMillis()
        db.tickrQueries.stopSession(now, now, sessionId)
    }

    override suspend fun deleteSession(sessionId: String) {
        db.tickrQueries.deleteSession(sessionId)
    }

    override suspend fun getTotalDurationForPeriod(tickrId: String, startTime: Long, endTime: Long): Long =
        db.tickrQueries.getTotalDurationForPeriod(currentTimeMillis(), tickrId, startTime, endTime).executeAsOne()

    override fun getAllActiveSessions(): Flow<List<DomainSession>> =
        db.tickrQueries.getActiveSessions().asFlow().mapToList(Dispatchers.IO).map { list -> list.map { it.toDomain() } }

    override fun getActiveSessions(): Flow<List<DomainSession>> =
        db.tickrQueries.getActiveSessions().asFlow().mapToList(Dispatchers.IO).map { list -> list.map { it.toDomain() } }
}

private fun com.victorarsjad.tickr.db.TimeSession.toDomain(): DomainSession =
    DomainSession(id = id, tickrId = tickrId, startedAt = startedAt, endedAt = endedAt, createdAt = createdAt, updatedAt = updatedAt)
