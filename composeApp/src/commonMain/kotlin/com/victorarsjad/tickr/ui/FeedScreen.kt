package com.victorarsjad.tickr.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.victorarsjad.tickr.domain.model.Period
import com.victorarsjad.tickr.domain.model.Tickr
import com.victorarsjad.tickr.domain.model.TickrType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: FeedViewModel, onNavigateToReport: (String) -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Tickr") }) },
        bottomBar = {
            CreationBar(
                name = state.creationFormState.name,
                type = state.creationFormState.type,
                period = state.creationFormState.period,
                error = state.creationFormState.error,
                onNameChange = viewModel::onUpdateCreationFormName,
                onTypeChange = viewModel::onUpdateCreationFormType,
                onPeriodChange = viewModel::onUpdateCreationFormPeriod,
                onCreate = viewModel::onCreateTickr
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.tickrs) { tickr ->
                TickrRow(
                    tickr = tickr,
                    isActive = state.activeSessions.containsKey(tickr.id),
                    currentValue = state.currentValues[tickr.id] ?: 0L,
                    onAction = { viewModel.onActionTickr(tickr) },
                    onClick = { onNavigateToReport(tickr.id) }
                )
            }
        }
    }
}

@Composable
private fun TickrRow(tickr: Tickr, isActive: Boolean, currentValue: Long, onAction: () -> Unit, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(600), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )
    val indicatorAlpha = if (isActive && tickr.type == TickrType.TIME) pulseAlpha else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = indicatorAlpha))
            )
            Spacer(modifier = Modifier.size(8.dp))
            val valueText = when (tickr.type) {
                TickrType.COUNT -> formatCountValue(currentValue)
                TickrType.TIME -> formatDuration(currentValue)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeIndicator(tickr.type)
                Spacer(modifier = Modifier.size(8.dp))
                PeriodIndicator(tickr.period)
                Spacer(modifier = Modifier.size(12.dp))
                Text(tickr.name)
                Spacer(modifier = Modifier.size(8.dp))
                Text(valueText)
            }
        }
        FilledIconButton(onClick = onAction, shape = CircleShape) {
            val icon = when (tickr.type) {
                TickrType.COUNT -> Icons.Filled.Add
                TickrType.TIME -> if (isActive) Icons.Filled.Pause else Icons.Filled.PlayArrow
            }
            Icon(icon, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreationBar(
    name: String,
    type: TickrType,
    period: Period,
    error: String?,
    onNameChange: (String) -> Unit,
    onTypeChange: (TickrType) -> Unit,
    onPeriodChange: (Period) -> Unit,
    onCreate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("What do you want to track today?") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                TypeSelector(selected = type, onSelect = onTypeChange)
                PeriodSelector(selected = period, onSelect = onPeriodChange)
            }
            Button(onClick = onCreate, shape = CircleShape) { Text("Create") }
        }
        if (!error.isNullOrBlank()) {
            Text("Error: $error")
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    val ms = (millis % 1000)
    return if (hours > 0) "%d:%02d:%02d.%03d".format(hours, minutes, seconds, ms) else "%02d:%02d.%03d".format(
        minutes,
        seconds,
        ms
    )
}

@Composable
private fun TypeSelector(selected: TickrType, onSelect: (TickrType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            TypeIndicator(selected)
            Spacer(modifier = Modifier.size(4.dp))
            Icon(
                if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TickrType.values().forEach { t ->
                DropdownMenuItem(onClick = { onSelect(t); expanded = false }, text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TypeIndicator(t)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(t.name)
                    }
                })
            }
        }
    }
}

@Composable
private fun PeriodSelector(selected: Period, onSelect: (Period) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            PeriodIndicator(selected)
            Spacer(modifier = Modifier.size(4.dp))
            Icon(
                if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Period.values().forEach { p ->
                DropdownMenuItem(onClick = { onSelect(p); expanded = false }, text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PeriodIndicator(p)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(p.name)
                    }
                })
            }
        }
    }
}

@Composable
private fun TypeIndicator(type: TickrType) {
    val icon = when (type) {
        TickrType.COUNT -> Icons.Filled.FormatListNumbered
        TickrType.TIME -> Icons.Filled.AccessTime
    }
    Icon(icon, contentDescription = null)
}

@Composable
private fun PeriodIndicator(period: Period) {
    val icon = when (period) {
        Period.DAY -> Icons.Filled.CalendarToday
        Period.WEEK -> Icons.Filled.CalendarViewWeek
        Period.MONTH -> Icons.Filled.CalendarMonth
        Period.LIFETIME -> Icons.Filled.AllInclusive
    }
    // TODO: Tint duration text while active
    Icon(icon, contentDescription = null)
}

private fun formatCountValue(value: Long): String = value.toString()
