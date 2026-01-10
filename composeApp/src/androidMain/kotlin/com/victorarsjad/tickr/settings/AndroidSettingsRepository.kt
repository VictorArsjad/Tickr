package com.victorarsjad.tickr.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.victorarsjad.tickr.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val DARK_THEME = booleanPreferencesKey("dark_theme")

val Context.settingsDataStore by preferencesDataStore(name = "settings")

class AndroidSettingsRepository(private val context: Context) : SettingsRepository {
    override val isDarkTheme: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[DARK_THEME] ?: false
    }

    override suspend fun setDarkTheme(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[DARK_THEME] = enabled
        }
    }
}
