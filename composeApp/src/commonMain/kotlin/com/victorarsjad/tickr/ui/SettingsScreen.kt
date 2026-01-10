package com.victorarsjad.tickr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.ui.tooling.preview.Preview

data class SettingsState(val isDarkTheme: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(state: SettingsState, onDarkThemeChange: (Boolean) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier.padding(inner).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dark Theme")
                Switch(checked = state.isDarkTheme, onCheckedChange = onDarkThemeChange)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    SettingsScreenContent(
        state = SettingsState(isDarkTheme = state.isDarkTheme),
        onDarkThemeChange = { viewModel.setDarkTheme(it) },
        onBack = onBack
    )
}

@Composable
@Preview
fun SettingsScreenContentPreview() {
    SettingsScreenContent(
        state = SettingsState(isDarkTheme = false),
        onDarkThemeChange = {},
        onBack = {}
    )
}