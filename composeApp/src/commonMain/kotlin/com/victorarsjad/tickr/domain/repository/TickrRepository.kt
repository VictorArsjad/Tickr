package com.victorarsjad.tickr.domain.repository

import com.victorarsjad.tickr.domain.model.CountValue
import com.victorarsjad.tickr.domain.model.Period
import com.victorarsjad.tickr.domain.model.Tickr
import kotlinx.coroutines.flow.Flow

interface TickrRepository {
    fun getAllTickrs(): Flow<List<Tickr>>
    fun getTickrById(id: String): Flow<Tickr>
    suspend fun createTickr(tickr: Tickr): Tickr
    suspend fun deleteTickr(id: String)
    suspend fun incrementCount(tickrId: String)
    suspend fun decrementCount(tickrId: String)
    suspend fun getCurrentCountForPeriod(tickrId: String): Int
    suspend fun checkAndResetTickrs(): List<String>
    suspend fun getCountValuesForPeriod(tickrId: String, startTime: Long, endTime: Long): List<CountValue>
}
