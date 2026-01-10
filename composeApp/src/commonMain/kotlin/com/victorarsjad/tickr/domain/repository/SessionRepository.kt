package com.victorarsjad.tickr.domain.repository

import com.victorarsjad.tickr.domain.model.TimeSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getSessionsForTickr(tickrId: String): Flow<List<TimeSession>>
    fun getActiveSessionForTickr(tickrId: String): Flow<TimeSession?>
    fun getSessionsForPeriod(tickrId: String, startTime: Long, endTime: Long): List<TimeSession>
    suspend fun createSession(session: TimeSession): TimeSession
    suspend fun stopSession(sessionId: String)
    suspend fun deleteSession(sessionId: String)
    suspend fun getTotalDurationForPeriod(tickrId: String, startTime: Long, endTime: Long): Long
    fun getAllActiveSessions(): Flow<List<TimeSession>>
    fun getActiveSessions(): Flow<List<TimeSession>>
}
