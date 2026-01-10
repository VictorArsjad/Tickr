package com.victorarsjad.tickr.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.victorarsjad.tickr.domain.model.Period
import com.victorarsjad.tickr.domain.model.Tickr
import com.victorarsjad.tickr.domain.model.TickrType
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration

data class FeedUiState(
    val tickrs: List<Tickr> = emptyList(),
    val activeSessions: Map<String, Any> = emptyMap(),
    val currentValues: Map<String, Long> = emptyMap(),
    val creationFormState: CreationFormState = CreationFormState()
)

data class CreationFormState(
    val name: String = "",
    val type: TickrType = TickrType.COUNT,
    val period: Period = Period.DAY,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreenContent(
    state: FeedUiState,
    onNavigateToReport: (String) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onTypeChange: (TickrType) -> Unit = {},
    onPeriodChange: (Period) -> Unit = {},
    onCreate: () -> Unit = {},
    onActionTickr: (Tickr) -> Unit = {},
    onTickrClick: (Tickr) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        text = "Tickr"
                    )
                },
                actions = {
                    IconButton(onClick = { onNavigateToReport("__settings__") }) {
                        Icon(Icons.Filled.Settings, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            CreationBar(
                name = state.creationFormState.name,
                type = state.creationFormState.type,
                period = state.creationFormState.period,
                error = state.creationFormState.error,
                onNameChange = onNameChange,
                onTypeChange = onTypeChange,
                onPeriodChange = onPeriodChange,
                onCreate = onCreate
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
                    onAction = { onActionTickr(tickr) },
                    onClick = { onTickrClick(tickr) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: FeedViewModel, onNavigateToReport: (String) -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    FeedScreenContent(
        state = FeedUiState(
            tickrs = state.tickrs,
            activeSessions = state.activeSessions,
            currentValues = state.currentValues,
            creationFormState = CreationFormState(
                name = state.creationFormState.name,
                type = state.creationFormState.type,
                period = state.creationFormState.period,
                error = state.creationFormState.error
            )
        ),
        onNavigateToReport = onNavigateToReport,
        onNameChange = viewModel::onUpdateCreationFormName,
        onTypeChange = viewModel::onUpdateCreationFormType,
        onPeriodChange = viewModel::onUpdateCreationFormPeriod,
        onCreate = viewModel::onCreateTickr,
        onActionTickr = viewModel::onActionTickr,
        onTickrClick = { onNavigateToReport(it.id) }
    )
}

@Composable
@Preview
fun FeedScreenContentPreview() {
    val now = 1894310400000L // 2026-01-10T00:00:00Z
    val mockTickrs = listOf(
        Tickr(
            id = "1",
            name = "Push-ups",
            type = TickrType.COUNT,
            period = Period.DAY,
            createdAt = now,
            updatedAt = now
        ),
        Tickr(
            id = "2",
            name = "Meditation",
            type = TickrType.TIME,
            period = Period.WEEK,
            createdAt = now,
            updatedAt = now
        )
    )
    FeedScreenContent(
        state = FeedUiState(
            tickrs = mockTickrs,
            activeSessions = mapOf("2" to Any()),
            currentValues = mapOf("1" to 15L, "2" to 60000L),
            creationFormState = CreationFormState(name = "", type = TickrType.COUNT, period = Period.DAY, error = null)
        ),
        onNavigateToReport = {},
        onNameChange = {},
        onTypeChange = {},
        onPeriodChange = {},
        onCreate = {},
        onActionTickr = {},
        onTickrClick = {}
    )
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                border = BorderStroke(
                    2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                ), shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypeIndicator(tickr.type)
                    Spacer(modifier = Modifier.size(8.dp))
                    PeriodIndicator(tickr.period)
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        text = tickr.name
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                Spacer(modifier = Modifier.size(8.dp))

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = indicatorAlpha))
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    style = MaterialTheme.typography.headlineLarge,
                    text = when (tickr.type) {
                        TickrType.COUNT -> formatCountValue(currentValue)
                        TickrType.TIME -> formatDuration(currentValue)
                    }
                )
                FilledIconButton(onClick = onAction, shape = CircleShape) {
                    val icon = when (tickr.type) {
                        TickrType.COUNT -> Icons.Filled.Add
                        TickrType.TIME -> if (isActive) Icons.Filled.Pause else Icons.Filled.PlayArrow
                    }
                    Icon(icon, contentDescription = null)
                }
            }

        }
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Box(
//                modifier = Modifier
//                    .size(10.dp)
//                    .clip(CircleShape)
//                    .background(MaterialTheme.colorScheme.primary.copy(alpha = indicatorAlpha))
//            )
//            Spacer(modifier = Modifier.size(8.dp))
//            val valueText = when (tickr.type) {
//                TickrType.COUNT -> formatCountValue(currentValue)
//                TickrType.TIME -> formatDuration(currentValue)
//            }
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                TypeIndicator(tickr.type)
//                Spacer(modifier = Modifier.size(8.dp))
//                PeriodIndicator(tickr.period)
//                Spacer(modifier = Modifier.size(12.dp))
//                Text(fontWeight = FontWeight.Bold, text=tickr.name)
//                Spacer(modifier = Modifier.size(8.dp))
//                Text(valueText)
//            }
//        }

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
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape
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
    return if (hours > 0) ("%d:%02d:%02d.%02d")
        .replace("%d", hours.toString())
        .replace("%02d", minutes.toString().padStart(2, '0'))
        .replace("%02d", seconds.toString().padStart(2, '0'))
        .replace("%02d", ms.toString().padStart(2, '0'))
    else ("%02d:%02d.%02d")
        .replace("%02d", minutes.toString().padStart(2, '0'))
        .replace("%02d", seconds.toString().padStart(2, '0'))
        .replace("%02d", ms.toString().padStart(2, '0'))
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
            TickrType.entries.forEach { t ->
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
            Period.entries.forEach { p ->
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
