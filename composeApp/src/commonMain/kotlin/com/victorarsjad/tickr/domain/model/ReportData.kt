package com.victorarsjad.tickr.domain.model

import kotlinx.datetime.LocalDate

data class DailyData(
    val date: LocalDate,
    val countValue: Int,
    val timeDurationMs: Long
)

data class ReportData(
    val tickrId: String,
    val tickrName: String,
    val type: TickrType,
    val period: Period,
    val dailyBreakdown: List<DailyData>,
    val totalValue: Long,
    val averagePerDay: Double
)
