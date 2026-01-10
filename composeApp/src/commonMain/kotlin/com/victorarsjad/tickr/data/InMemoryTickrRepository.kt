package com.victorarsjad.tickr.data

// TODO: Replace with SQLDelight-backed implementation per platform. Android uses SqlDelightTickrRepository.

import com.victorarsjad.tickr.domain.model.CountValue
import com.victorarsjad.tickr.domain.model.Period
import com.victorarsjad.tickr.domain.model.Tickr
import com.victorarsjad.tickr.domain.model.TickrType
import com.victorarsjad.tickr.domain.repository.TickrRepository
import com.victorarsjad.tickr.util.UUIDGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class InMemoryTickrRepository(
    private val uuidGenerator: UUIDGenerator
) : TickrRepository {
    private val mutex = Mutex()
    private val tickrsFlow = MutableStateFlow<List<Tickr>>(emptyList())
    private val countValues = mutableListOf<CountValue>()

    override fun getAllTickrs(): Flow<List<Tickr>> = tickrsFlow

    override fun getTickrById(id: String): Flow<Tickr> = tickrsFlow.map { list ->
        list.first { it.id == id }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun createTickr(tickr: Tickr): Tickr = mutex.withLock {
        val now = Clock.System.now().toEpochMilliseconds()
        val newTickr = tickr.copy(updatedAt = now)
        tickrsFlow.value += newTickr
        if (newTickr.type == TickrType.COUNT) {
            val start = newTickr.period.calculateBoundaries(now).first
            countValues.add(
                CountValue(
                    id = uuidGenerator.generate(),
                    tickrId = newTickr.id,
                    value = 0,
                    periodStartAt = start,
                    periodEndAt = null,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
        newTickr
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun deleteTickr(id: String) = mutex.withLock {
        val now = Clock.System.now().toEpochMilliseconds()
        tickrsFlow.value = tickrsFlow.value.map {
            if (it.id == id) it.copy(isDeleted = true, updatedAt = now) else it
        }
    }

    private fun getCurrentCountRow(tickrId: String): CountValue? =
        countValues.firstOrNull { it.tickrId == tickrId && it.periodEndAt == null }

    private fun archiveCountRow(row: CountValue, endAt: Long) {
        val idx = countValues.indexOfFirst { it.id == row.id }
        if (idx >= 0) countValues[idx] = row.copy(periodEndAt = endAt, updatedAt = endAt)
    }

    private fun ensureCurrentRowForPeriod(tickr: Tickr, now: Long): CountValue {
        val current = getCurrentCountRow(tickr.id)
        val (start, end) = tickr.period.calculateBoundaries(now)
        if (current == null) {
            val newRow = CountValue(
                id = uuidGenerator.generate(),
                tickrId = tickr.id,
                value = 0,
                periodStartAt = start,
                periodEndAt = null,
                createdAt = now,
                updatedAt = now
            )
            countValues.add(newRow)
            return newRow
        }
        if (current.periodStartAt != start) {
            archiveCountRow(current, end)
            val newRow = CountValue(
                id = uuidGenerator.generate(),
                tickrId = tickr.id,
                value = 0,
                periodStartAt = start,
                periodEndAt = null,
                createdAt = now,
                updatedAt = now
            )
            countValues.add(newRow)
            return newRow
        }
        return current
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun incrementCount(tickrId: String) = mutex.withLock {
        val now = Clock.System.now().toEpochMilliseconds()
        val tickr = getTickrById(tickrId).first()
        val row = ensureCurrentRowForPeriod(tickr, now)
        val idx = countValues.indexOfFirst { it.id == row.id }
        countValues[idx] = row.copy(value = row.value + 1, updatedAt = now)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun decrementCount(tickrId: String) = mutex.withLock {
        val now = Clock.System.now().toEpochMilliseconds()
        val row = getCurrentCountRow(tickrId) ?: return@withLock
        val idx = countValues.indexOfFirst { it.id == row.id }
        val newVal = (row.value - 1).coerceAtLeast(0)
        countValues[idx] = row.copy(value = newVal, updatedAt = now)
    }

    override suspend fun getCurrentCountForPeriod(tickrId: String): Int = mutex.withLock {
        getCurrentCountRow(tickrId)?.value ?: 0
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun checkAndResetTickrs(): List<String> = mutex.withLock {
        val now = Clock.System.now().toEpochMilliseconds()
        val resetIds = mutableListOf<String>()
        tickrsFlow.value.forEach { t ->
            if (t.type == TickrType.COUNT) {
                val row = getCurrentCountRow(t.id) ?: return@forEach
                val start = t.period.calculateBoundaries(now).first
                if (row.periodStartAt != start) {
                    val end = t.period.calculateBoundaries(row.periodStartAt).second
                    archiveCountRow(row, end)
                    val newRow = CountValue(
                        id = uuidGenerator.generate(),
                        tickrId = t.id,
                        value = 0,
                        periodStartAt = start,
                        periodEndAt = null,
                        createdAt = now,
                        updatedAt = now
                    )
                    countValues.add(newRow)
                    resetIds.add(t.id)
                }
            }
        }
        resetIds
    }

    override suspend fun getCountValuesForPeriod(tickrId: String, startTime: Long, endTime: Long): List<CountValue> = mutex.withLock {
        countValues.filter { it.tickrId == tickrId && it.periodStartAt >= startTime && it.periodStartAt < endTime }
    }
}
