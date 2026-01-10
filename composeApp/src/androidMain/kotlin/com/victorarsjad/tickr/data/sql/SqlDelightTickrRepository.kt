package com.victorarsjad.tickr.data.sql

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.victorarsjad.tickr.db.TickrDatabase
import com.victorarsjad.tickr.domain.model.CountValue as DomainCountValue
import com.victorarsjad.tickr.domain.model.Period
import com.victorarsjad.tickr.domain.model.Tickr as DomainTickr
import com.victorarsjad.tickr.domain.model.TickrType
import com.victorarsjad.tickr.domain.repository.TickrRepository
import com.victorarsjad.tickr.util.UUIDGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.System.currentTimeMillis

class SqlDelightTickrRepository(
    private val db: TickrDatabase,
    private val uuidGenerator: UUIDGenerator
) : TickrRepository {

    override fun getAllTickrs(): Flow<List<DomainTickr>> =
        db.tickrQueries.getAllTickrs().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toDomain() }
        }

    override fun getTickrById(id: String): Flow<DomainTickr> =
        db.tickrQueries.getTickrById(id).asFlow().mapToOne(Dispatchers.IO).map { it.toDomain() }

    override suspend fun createTickr(tickr: DomainTickr): DomainTickr {
        val now = currentTimeMillis()
        db.tickrQueries.insertTickr(
            tickr.id,
            tickr.name,
            tickr.type.name,
            tickr.period.name,
            tickr.createdAt,
            now
        )
        if (tickr.type == TickrType.COUNT) {
            val start = tickr.period.calculateBoundaries(now).first
            db.tickrQueries.insertCountValue(
                uuidGenerator.generate(),
                tickr.id,
                0L,
                start,
                now,
                now
            )
        }
        return tickr.copy(updatedAt = now)
    }

    override suspend fun deleteTickr(id: String) {
        db.tickrQueries.softDeleteTickr(currentTimeMillis(), id)
    }

    override suspend fun incrementCount(tickrId: String) {
        val now = currentTimeMillis()
        val tickr = db.tickrQueries.getTickrById(tickrId).executeAsOne()
        val (start, end) = Period.valueOf(tickr.period).calculateBoundaries(now)
        val current = db.tickrQueries.getCurrentCountValue(tickrId).executeAsOneOrNull()
        if (current == null) {
            db.tickrQueries.insertCountValue(
                uuidGenerator.generate(),
                tickrId,
                1L,
                start,
                now,
                now
            )
        } else if (current.periodStartAt != start) {
            db.tickrQueries.archiveCountValue(end, now, current.id)
            db.tickrQueries.insertCountValue(
                uuidGenerator.generate(),
                tickrId,
                1L,
                start,
                now,
                now
            )
        } else {
            db.tickrQueries.incrementCountValue(now, current.id)
        }
    }

    override suspend fun decrementCount(tickrId: String) {
        val current = db.tickrQueries.getCurrentCountValue(tickrId).executeAsOneOrNull() ?: return
        val now = currentTimeMillis()
        val newVal = (current.value_ - 1).coerceAtLeast(0)
        db.tickrQueries.updateCountValue(newVal.toLong(), now, current.id)
    }

    override suspend fun getCurrentCountForPeriod(tickrId: String): Int =
        db.tickrQueries.getCurrentCountValue(tickrId).executeAsOneOrNull()?.value_?.toInt() ?: 0

    override suspend fun checkAndResetTickrs(): List<String> {
        val now = currentTimeMillis()
        val reset = mutableListOf<String>()
        db.transaction {
            db.tickrQueries.getAllTickrs().executeAsList().forEach { t ->
                if (TickrType.valueOf(t.type) == TickrType.COUNT) {
                    val (start, end) = Period.valueOf(t.period).calculateBoundaries(now)
                    val current = db.tickrQueries.getCurrentCountValue(t.id).executeAsOneOrNull() ?: return@forEach
                    if (current.periodStartAt != start) {
                        db.tickrQueries.archiveCountValue(end, now, current.id)
                        db.tickrQueries.insertCountValue(
                            uuidGenerator.generate(),
                            t.id,
                            0L,
                            start,
                            now,
                            now
                        )
                        reset.add(t.id)
                    }
                }
            }
        }
        return reset
    }

    override suspend fun getCountValuesForPeriod(tickrId: String, startTime: Long, endTime: Long): List<DomainCountValue> =
        db.tickrQueries.getAllCountValuesForTickr(tickrId).executeAsList()
            .filter { it.periodStartAt >= startTime && (it.periodEndAt == null || it.periodEndAt < endTime) }
            .map { DomainCountValue(it.id, it.tickrId, it.value_?.toInt() ?: 0, it.periodStartAt, it.periodEndAt, it.createdAt, it.updatedAt) }
}

private fun com.victorarsjad.tickr.db.Tickr.toDomain(): DomainTickr =
    DomainTickr(
        id = id,
        name = name,
        type = TickrType.valueOf(type),
        period = Period.valueOf(period),
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted != 0L
    )
