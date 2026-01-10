package com.victorarsjad.tickr.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.victorarsjad.tickr.domain.model.Period
import com.victorarsjad.tickr.domain.model.TickrType

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
    Scaffold(
        topBar = {
            TopBar(
                title = state.reportData?.tickrName ?: "Report",
                selectedPeriod = state.selectedPeriod,
                onPeriodChange = { viewModel.changePeriod(it) },
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
                        Text("Average/day: ${String.format("%.2f", data.averagePerDay)}")
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
        actions = {
            PeriodSelector(selected = selectedPeriod, onSelect = onPeriodChange)
            Button(onClick = onBack) { Text("Back") }
        }
    )
}

@Composable
private fun PeriodSelector(selected: Period, onSelect: (Period) -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    Button(onClick = { expanded.value = true }) { Text(selected.name) }
    DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
        Period.values().forEach { p ->
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
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
