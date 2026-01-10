package com.victorarsjad.tickr.domain.model

import com.victorarsjad.tickr.util.PeriodUtils
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class Tickr(
    val id: String,
    val name: String,
    val type: TickrType,
    val period: Period,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)

enum class TickrType { COUNT, TIME }

enum class Period {
    DAY, WEEK, MONTH, LIFETIME;

    fun nextResetTime(fromMillis: Long): Long = when (this) {
        DAY -> PeriodUtils.nextMidnightMillis(fromMillis)
        WEEK -> PeriodUtils.nextMondayMidnightMillis(fromMillis)
        MONTH -> PeriodUtils.firstOfNextMonthMidnightMillis(fromMillis)
        LIFETIME -> Long.MAX_VALUE
    }

    @OptIn(ExperimentalTime::class)
    fun calculateBoundaries(referenceMillis: Long = Clock.System.now().toEpochMilliseconds()): Pair<Long, Long> = when (this) {
        DAY -> PeriodUtils.dayBounds(referenceMillis)
        WEEK -> PeriodUtils.weekBounds(referenceMillis)
        MONTH -> PeriodUtils.monthBounds(referenceMillis)
        LIFETIME -> 0L to Long.MAX_VALUE
    }
}

@OptIn(ExperimentalTime::class)
fun Long.toLocalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone).date
