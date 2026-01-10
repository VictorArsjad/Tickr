package com.victorarsjad.tickr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.victorarsjad.tickr.domain.model.Period
import com.victorarsjad.tickr.domain.model.TickrType
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ReportScreenContent(
    state: ReportUiState,
    onPeriodChange: (Period) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
                title = state.reportData?.tickrName ?: "Report",
                selectedPeriod = state.selectedPeriod,
                onPeriodChange = onPeriodChange,
                onBack = onBack
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                state.isLoading -> Text("Loading…")
                state.error != null -> Text("Error: ${state.error}")
                else -> {
                    val data = state.reportData
                    if (data != null) {
                        Text("Total: ${formatValue(data.totalValue, data.type)}")
                        Text("Average/day: " + data.averagePerDay.toString())
                        LazyColumn(
                            contentPadding = PaddingValues(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(data.dailyBreakdown) { day ->
                                val valueText = when (data.type) {
                                    TickrType.COUNT -> day.countValue.toString()
                                    TickrType.TIME -> formatValue(day.timeDurationMs, data.type)
                                }
                                Text("${day.date}: $valueText")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    tickrId: String,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(tickrId) {
        if (state.reportData?.tickrId != tickrId) {
            viewModel.loadReport(tickrId, state.selectedPeriod)
        }
    }
    ReportScreenContent(
        state = ReportUiState(
            isLoading = state.isLoading,
            error = state.error,
            reportData = state.reportData?.let {
                ReportData(
                    tickrId = it.tickrId,
                    tickrName = it.tickrName,
                    totalValue = it.totalValue,
                    averagePerDay = it.averagePerDay,
                    type = it.type,
                    dailyBreakdown = it.dailyBreakdown.map { d ->
                        DailyBreakdown(
                            date = d.date.toString(),
                            countValue = d.countValue.toLong(),
                            timeDurationMs = d.timeDurationMs
                        )
                    }
                )
            },
            selectedPeriod = state.selectedPeriod
        ),
        onPeriodChange = { viewModel.changePeriod(it) },
        onBack = onBack
    )
}

data class ReportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val reportData: ReportData? = null,
    val selectedPeriod: Period = Period.DAY
)

data class ReportData(
    val tickrId: String,
    val tickrName: String,
    val totalValue: Long,
    val averagePerDay: Double,
    val type: TickrType,
    val dailyBreakdown: List<DailyBreakdown>
)

data class DailyBreakdown(
    val date: String,
    val countValue: Long = 0,
    val timeDurationMs: Long = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    selectedPeriod: Period,
    onPeriodChange: (Period) -> Unit,
    onBack: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            PeriodSelector(selected = selectedPeriod, onSelect = onPeriodChange)
        }
    )
}

@Composable
private fun PeriodSelector(selected: Period, onSelect: (Period) -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    Button(onClick = { expanded.value = true }) { Text(selected.name) }
    DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
        Period.entries.forEach { p ->
            DropdownMenuItem(onClick = { onSelect(p); expanded.value = false }, text = { Text(p.name) })
        }
    }
}

private fun formatValue(value: Long, type: TickrType): String =
    when (type) {
        TickrType.COUNT -> value.toString()
        TickrType.TIME -> formatDuration(value)
    }

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return (if (hours < 10) "0$hours" else "$hours") + ":" + (if (minutes < 10) "0$minutes" else "$minutes") + ":" + (if (seconds < 10) "0$seconds" else "$seconds")
}

@Composable
@Preview
fun ReportScreenContentPreview() {
    val mockData = ReportData(
        tickrId = "1",
        tickrName = "Sample Tickr",
        totalValue = 1234,
        averagePerDay = 56.78,
        type = TickrType.COUNT,
        dailyBreakdown = listOf(
            DailyBreakdown(date = "2026-01-01", countValue = 10),
            DailyBreakdown(date = "2026-01-02", countValue = 20)
        )
    )
    ReportScreenContent(
        state = ReportUiState(
            isLoading = false,
            error = null,
            reportData = mockData,
            selectedPeriod = Period.DAY
        ),
        onPeriodChange = {},
        onBack = {}
    )
}
