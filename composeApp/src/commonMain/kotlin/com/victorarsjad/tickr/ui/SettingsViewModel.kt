package com.victorarsjad.tickr.ui

import com.victorarsjad.tickr.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) {
    data class State(
        val isDarkTheme: Boolean = false
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        scope.launch {
            settingsRepository.isDarkTheme.collect { enabled ->
                _state.update { it.copy(isDarkTheme = enabled) }
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        scope.launch {
            settingsRepository.setDarkTheme(enabled)
        }
    }
}
