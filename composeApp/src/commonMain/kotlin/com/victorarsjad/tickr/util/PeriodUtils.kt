package com.victorarsjad.tickr.util

import kotlinx.datetime.*

object PeriodUtils {
    fun nextMidnightMillis(fromMillis: Long): Long {
        val tz = TimeZone.currentSystemDefault()
        val from = Instant.fromEpochMilliseconds(fromMillis).toLocalDateTime(tz)
        val nextDayStart = LocalDateTime(date = from.date.plus(DatePeriod(days = 1)), time = LocalTime(0, 0))
        return nextDayStart.toInstant(tz).toEpochMilliseconds()
    }

    fun nextMondayMidnightMillis(fromMillis: Long): Long {
        val tz = TimeZone.currentSystemDefault()
        val from = Instant.fromEpochMilliseconds(fromMillis).toLocalDateTime(tz)
        val daysToAdd = ((DayOfWeek.MONDAY.ordinal - from.date.dayOfWeek.ordinal + 7) % 7).let { if (it == 0) 7 else it }
        val nextMonday = from.date.plus(DatePeriod(days = daysToAdd))
        val nextMondayStart = LocalDateTime(date = nextMonday, time = LocalTime(0, 0))
        return nextMondayStart.toInstant(tz).toEpochMilliseconds()
    }

    fun firstOfNextMonthMidnightMillis(fromMillis: Long): Long {
        val tz = TimeZone.currentSystemDefault()
        val from = Instant.fromEpochMilliseconds(fromMillis).toLocalDateTime(tz)
        val year = if (from.date.monthNumber == 12) from.date.year + 1 else from.date.year
        val month = if (from.date.monthNumber == 12) 1 else from.date.monthNumber + 1
        val firstNextMonth = LocalDate(year, month, 1)
        val start = LocalDateTime(firstNextMonth, LocalTime(0, 0))
        return start.toInstant(tz).toEpochMilliseconds()
    }

    fun dayBounds(referenceMillis: Long): Pair<Long, Long> {
        val tz = TimeZone.currentSystemDefault()
        val dateTime = Instant.fromEpochMilliseconds(referenceMillis).toLocalDateTime(tz)
        val start = LocalDateTime(dateTime.date, LocalTime(0, 0)).toInstant(tz).toEpochMilliseconds()
        val end = nextMidnightMillis(referenceMillis)
        return start to end
    }

    fun weekBounds(referenceMillis: Long): Pair<Long, Long> {
        val tz = TimeZone.currentSystemDefault()
        val dateTime = Instant.fromEpochMilliseconds(referenceMillis).toLocalDateTime(tz)
        val daysSinceMonday = (dateTime.date.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
        val monday = dateTime.date.minus(DatePeriod(days = daysSinceMonday))
        val start = LocalDateTime(monday, LocalTime(0, 0)).toInstant(tz).toEpochMilliseconds()
        val end = nextMondayMidnightMillis(referenceMillis)
        return start to end
    }

    fun monthBounds(referenceMillis: Long): Pair<Long, Long> {
        val tz = TimeZone.currentSystemDefault()
        val dateTime = Instant.fromEpochMilliseconds(referenceMillis).toLocalDateTime(tz)
        val firstOfMonth = LocalDate(dateTime.date.year, dateTime.date.monthNumber, 1)
        val start = LocalDateTime(firstOfMonth, LocalTime(0, 0)).toInstant(tz).toEpochMilliseconds()
        val end = firstOfNextMonthMidnightMillis(referenceMillis)
        return start to end
    }
}
