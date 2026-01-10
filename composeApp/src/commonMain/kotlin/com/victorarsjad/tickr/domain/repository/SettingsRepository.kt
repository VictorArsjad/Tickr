package com.victorarsjad.tickr.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val isDarkTheme: Flow<Boolean>
    suspend fun setDarkTheme(enabled: Boolean)
}
