package com.victorarsjad.tickr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.android.ext.android.inject
import com.victorarsjad.tickr.ui.FeedScreen
import com.victorarsjad.tickr.ui.FeedViewModel
import com.victorarsjad.tickr.ui.ReportScreen
import com.victorarsjad.tickr.ui.ReportViewModel
import com.victorarsjad.tickr.domain.model.Period
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.victorarsjad.tickr.ui.theme.TickrTheme
import com.victorarsjad.tickr.domain.repository.SettingsRepository
import androidx.compose.runtime.collectAsState
import com.victorarsjad.tickr.ui.SettingsScreen
import com.victorarsjad.tickr.ui.SettingsViewModel
import androidx.activity.compose.BackHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val feedVm: FeedViewModel by inject()
        val reportVm: ReportViewModel by inject()
        val settings: SettingsRepository by inject()
        val settingsVm: SettingsViewModel by inject()
        setContent {
            val dark by settings.isDarkTheme.collectAsState(initial = false)
            TickrTheme(darkTheme = dark) {
                var route by remember { mutableStateOf<Screen>(Screen.Feed) }
                BackHandler(enabled = route is Screen.Report || route is Screen.Settings) {
                    route = Screen.Feed
                }
                when (val r = route) {
                    is Screen.Feed -> FeedScreen(feedVm, onNavigateToReport = { id ->
                        if (id == "__settings__") {
                            route = Screen.Settings
                        } else {
                            route = Screen.Report(id)
                            reportVm.loadReport(id, Period.DAY)
                        }
                    })
                    is Screen.Report -> ReportScreen(reportVm, r.tickrId, onBack = { route = Screen.Feed })
                    is Screen.Settings -> SettingsScreen(settingsVm, onBack = { route = Screen.Feed })
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

private sealed class Screen {
    data object Feed : Screen()
    data class Report(val tickrId: String) : Screen()
    data object Settings : Screen()
}