package com.victorarsjad.tickr.domain.usecase

import com.victorarsjad.tickr.domain.model.CountValue
import com.victorarsjad.tickr.domain.model.DailyData
import com.victorarsjad.tickr.domain.model.Period
import com.victorarsjad.tickr.domain.model.ReportData
import com.victorarsjad.tickr.domain.model.TickrType
import com.victorarsjad.tickr.domain.model.TimeSession
import com.victorarsjad.tickr.domain.repository.SessionRepository
import com.victorarsjad.tickr.domain.repository.TickrRepository
import com.victorarsjad.tickr.domain.model.toLocalDate
import kotlinx.coroutines.flow.first

class GetReportDataUseCase(
    private val tickrRepository: TickrRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(tickrId: String, period: Period): ReportData {
        val tickr = tickrRepository.getTickrById(tickrId).first()
        val (startTime, endTime) = period.calculateBoundaries()
        return when (tickr.type) {
            TickrType.COUNT -> {
                val countValues = tickrRepository.getCountValuesForPeriod(tickrId, startTime, endTime)
                aggregateCountData(tickr.id, tickr.name, tickr.type, tickr.period, countValues)
            }
            TickrType.TIME -> {
                val sessions = sessionRepository.getSessionsForPeriod(tickrId, startTime, endTime)
                aggregateTimeData(tickr.id, tickr.name, tickr.type, tickr.period, sessions)
            }
        }
    }

    private fun aggregateCountData(
        tickrId: String,
        tickrName: String,
        type: TickrType,
        period: Period,
        values: List<CountValue>
    ): ReportData {
        val grouped = values.groupBy { it.periodStartAt.toLocalDate() }
        val dailyBreakdown = grouped.map { (date, dayValues) ->
            DailyData(
                date = date,
                countValue = dayValues.sumOf { it.value },
                timeDurationMs = 0L
            )
        }.sortedByDescending { it.date }
        val total = dailyBreakdown.sumOf { it.countValue.toLong() }
        val average = if (dailyBreakdown.isNotEmpty()) total.toDouble() / dailyBreakdown.size else 0.0
        return ReportData(
            tickrId = tickrId,
            tickrName = tickrName,
            type = type,
            period = period,
            dailyBreakdown = dailyBreakdown,
            totalValue = total,
            averagePerDay = average
        )
    }

    private fun aggregateTimeData(
        tickrId: String,
        tickrName: String,
        type: TickrType,
        period: Period,
        sessions: List<TimeSession>
    ): ReportData {
        val grouped = sessions.groupBy { it.startedAt.toLocalDate() }
        val dailyBreakdown = grouped.map { (date, daySessions) ->
            DailyData(
                date = date,
                countValue = 0,
                timeDurationMs = daySessions.sumOf { it.duration }
            )
        }.sortedByDescending { it.date }
        val total = dailyBreakdown.sumOf { it.timeDurationMs }
        val average = if (dailyBreakdown.isNotEmpty()) total.toDouble() / dailyBreakdown.size else 0.0
        return ReportData(
            tickrId = tickrId,
            tickrName = tickrName,
            type = type,
            period = period,
            dailyBreakdown = dailyBreakdown,
            totalValue = total,
            averagePerDay = average
        )
    }
}
